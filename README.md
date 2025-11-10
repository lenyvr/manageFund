This project is about clients managing subscriptions and cancelations from a funds list. 
1) Each client has a initial amount of 500.000
2) When a client subscribe to a fund, a comunication is sent according to the client election between email or text message.
3) Each fund have a minimun amount subscription, this means thet if a client haven't enought amount can not be subscribed.
4) When a client cancel the subscription, the subscription amount will be refunded.

This project was created with Java 17, Spring boot 3 and aws sdk 2.
You can use maven commands like clean, build,install and package.
Once deployed you can find the endpoints documentations in http://localhost:8080/swagger-ui/index.html
