docker-compose -f docker-compose-single-broker.yml up -d
docker-compose -f docker-compose-swarm.yml up -d

kafkacat -P -b radek-VirtualBox:19092 -X api.version.request=true -K '#' -t mmy
kafkacat -C -b radek-VirtualBox:19092 -K '#' -t mmy