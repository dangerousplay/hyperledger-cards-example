package org.dangerousplay.hyperledger.cards.configuration

import org.hyperledger.fabric.gateway.*
import org.hyperledger.fabric.sdk.Enrollment
import org.hyperledger.fabric.sdk.User
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest
import org.hyperledger.fabric_ca.sdk.HFCAClient
import org.hyperledger.fabric_ca.sdk.RegistrationRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.nio.file.Paths
import java.security.PrivateKey
import java.util.*


@Configuration
open class FabricConfiguration {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun createUsers() {
        // Create a CA client for interacting with the CA.
        val props = Properties()

        val pem = ClassPathResource("ca.org1.example.com-cert.pem")

        props["pemBytes"] = pem.inputStream.readAllBytes()

        props["allowAllHostNames"] = "true"

        val caClient = HFCAClient.createNewInstance("https://localhost:7054", props)

        val cryptoSuite = CryptoSuiteFactory.getDefault().cryptoSuite
        caClient.cryptoSuite = cryptoSuite

        // Create a wallet for managing identities
        val wallet = Wallets.newFileSystemWallet(Paths.get("wallet"))

        // Check to see if we've already enrolled the admin user.
        if (wallet["admin"] != null) {
            logger.info("An identity for the admin user \"admin\" already exists in the wallet")
        } else {
            // Enroll the admin user, and import the new identity into the wallet.
            val enrollmentRequestTLS = EnrollmentRequest()
            enrollmentRequestTLS.addHost("localhost")
            enrollmentRequestTLS.profile = "tls"

            val adminEnrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS)

            val adminUser: Identity =
                Identities.newX509Identity("Org1MSP", adminEnrollment)

            wallet.put("admin", adminUser)

            logger.info("Successfully enrolled user \"admin\" and imported it into the wallet")
        }



        if (wallet["appUser"] != null) {
            println("An identity for the user \"appUser\" already exists in the wallet")
            return
        }

        val adminIdentity =
            wallet["admin"] as X509Identity

        val admin: User = object : User {
            override fun getName(): String {
                return "admin"
            }

            override fun getRoles(): Set<String>? {
                return null
            }

            override fun getAccount(): String? {
                return null
            }

            override fun getAffiliation(): String {
                return "org1.department1"
            }

            override fun getEnrollment(): Enrollment {
                return object : Enrollment {
                    override fun getKey(): PrivateKey {
                        return adminIdentity.privateKey
                    }

                    override fun getCert(): String {
                        return Identities.toPemString(adminIdentity.certificate)
                    }
                }
            }

            override fun getMspId(): String {
                return "Org1MSP"
            }
        }

        // Register the user, enroll the user, and import the new identity into the wallet.
        val registrationRequest = RegistrationRequest("appUser")
        registrationRequest.affiliation = "org1.department1"
        registrationRequest.enrollmentID = "appUser"

        val enrollmentSecret = caClient.register(registrationRequest, admin)

        val enrollment = caClient.enroll("appUser", enrollmentSecret)

        val user: Identity =
            Identities.newX509Identity("Org1MSP", enrollment)

        wallet.put("appUser", user)

        logger.info("Successfully enrolled user \"appUser\" and imported it into the wallet")
    }


    @Bean
    open fun connect(
        @Value("classpath:connection-org1.yaml") configFile: Resource
    ): Gateway {

        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true")

        this.createUsers()

        // Load a file system based wallet for managing identities.
        val walletPath = Paths.get("wallet")

        val wallet = Wallets.newFileSystemWallet(walletPath)


        return Gateway.createBuilder()
            .identity(wallet, "appUser")
            .networkConfig(configFile.inputStream)
            .discovery(true)
            .connect()
    }


}
