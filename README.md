Source Code for client and server
 
    https://github.com/jeganth1/Adpumb-Project.git

To get Docker images Uploaded in Docker hub
      
    Proxy Client - sudo docker pull jeganth/adpubm_server
    proxy server - sudo docker pull jeganth/adpumb_client


To Run Docker Containers for both client and Server

    Proxy Client Server - sudo docker run -d --rm --name proxy-server -p 9090:9090 jeganth/adpubm_server
    Proxy Client container - sudo docker run -d --rm --name proxy-client --link proxy-server -p 8080:8080 jeganth/adpumb_client
