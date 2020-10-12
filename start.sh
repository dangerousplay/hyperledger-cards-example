# launch network; create channel and join peer to channel

rm -rf ./wallet
cd ./test-network
./network.sh down
./network.sh up createChannel -ca -s couchdb
