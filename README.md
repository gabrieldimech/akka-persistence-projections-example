# Event Sourcing with Spring Boot / Akka Peristence / Projections
This example is intended to show how to leverage AKKA persistence as a tool to implement [event sourcing](https://doc.akka.io/docs/akka/current/typed/persistence.html) in an API. Tio demonstrate this, the application consumes kafka messages (this could easily be swapped with HTTP requets directly), and leverages AKKA persistence to append events to an event store. AKKA persistence will handle the management of the event store management which in this case is configrured to persist all relevant management data in Mysql. A projection was added to the application in order to persist events in the mysql databse too. This serves as the write model of a CQRS app, hence adding a read model that loads this Mysql data (or better add a projection to another database which can handle quick reads) will add the benefit of having a well designed CQRS app where the write and read models are separated. 

# Running the app
Using the maven commaned below will build and deploy the project:

```./mvnw spring-boot:run```

# Endpoints
## Get news for topics 
Accepts a colelcion of topics and returns a JSON response with news items for each topic.

The demo ednpoint is available to test [here](https://ancient-river-40675.herokuapp.com/news).

### POST localhost:8080/news
Request Payload example:

```
{
   "topics":[
      "FACEBOOK",
      "TESLA",
      "GOOGLE",
      "TWITTER",
      "NIO",
      "COINBASE",
      "AMD",
      "NVIDIA",
      "AMC",
      "AMAZON"
   ]
}
```

Sample response:
```
{
    "FACEBOOK": [
        {
            "title": "Facebook account hacked? Here’s how to find out instantly and recover it - India Today",
            "link": "https://www.indiatoday.in/information/story/facebook-account-hacked-how-to-find-out-and-instantly-recover-it-1835755-2021-08-02",
            "description": "FACEBOOK",
            "topic": "FACEBOOK",
            "pubDate": "2021-08-02T09:33:52.000+00:00"
        }
    ],
    "AMAZON": [
        {
            "title": "Amazon Taps Cleopatra and Rapunzel to Convince Us That ‘Prime Changes Everything’ - Adweek",
            "link": "https://www.adweek.com/brand-marketing/amazon-prime-subscription-cleopatra-campaign/",
            "description": "AMAZON",
            "topic": "AMAZON",
            "pubDate": "2021-08-02T09:59:59.000+00:00"
        }
    ],
    "NVIDIA": [
        {
            "title": "Nvidia’s RTX 4000 GPUs promise a huge performance leap – but AMD RDNA 3 might outdo them - TechRadar",
            "link": "https://www.techradar.com/news/nvidias-rtx-4000-gpus-promise-a-huge-performance-leap-but-amd-rdna-3-might-outdo-them",
            "description": "NVIDIA",
            "topic": "NVIDIA",
            "pubDate": "2021-08-02T09:54:00.000+00:00"
        }
    ],
    "COINBASE": [
        {
            "title": "Winklevoss twins run into British crypto trouble - Telegraph.co.uk",
            "link": "https://www.telegraph.co.uk/technology/2021/08/02/winklevoss-twins-run-british-crypto-trouble/",
            "description": "COINBASE",
            "topic": "COINBASE",
            "pubDate": "2021-08-02T10:18:00.000+00:00"
        }
    ],
    "NIO": [
        {
            "title": "Nio July M/M sales tick lower to 7,931, XPEV and LI registers M/M growth - Seeking Alpha",
            "link": "https://seekingalpha.com/news/3722783-nio-july-sales-tick-lower-mm-to-7931-vehicles-xpev-and-li-registers-mm-growth",
            "description": "NIO",
            "topic": "NIO",
            "pubDate": "2021-08-02T09:40:00.000+00:00"
        }
    ],
    "TESLA": [
        {
            "title": "“I F--king Hope He Sues Me”: Inside the Twitter Explosion that Nearly Sunk Elon Musk - Vanity Fair",
            "link": "https://www.vanityfair.com/news/2021/07/inside-the-twitter-explosion-that-nearly-sunk-elon-musk-tesla-power-play-excerpt",
            "description": "TESLA",
            "topic": "TESLA",
            "pubDate": "2021-08-02T09:55:00.000+00:00"
        }
    ],
    "AMC": [
        {
            "title": "AMC, Tilray, Carnival, Alibaba, Apple, Facebook — Stocks New Jersey Pension Fund Bought And Sold In Q2 - - Benzinga",
            "link": "https://www.benzinga.com/markets/cannabis/21/08/22269221/amc-tilray-carnival-alibaba-apple-facebook-stocks-new-jersey-pension-fund-bought-and-sold-in-q2",
            "description": "AMC",
            "topic": "AMC",
            "pubDate": "2021-08-02T09:54:00.000+00:00"
        }
    ],
    "AMD": [
        {
            "title": "Latest Steam survey shows AMD rebounding, an excellent month for Ampere, and a Windows 7 resurgence - TechSpot",
            "link": "https://www.techspot.com/news/90625-steam-latest-hardware-survey-shows-amd-rebounding-excellent.html",
            "description": "AMD",
            "topic": "AMD",
            "pubDate": "2021-08-02T10:15:00.000+00:00"
        }
    
    ],
    "TWITTER": [
        {
            "title": "“I F--king Hope He Sues Me”: Inside the Twitter Explosion that Nearly Sunk Elon Musk - Vanity Fair",
            "link": "https://www.vanityfair.com/news/2021/07/inside-the-twitter-explosion-that-nearly-sunk-elon-musk-tesla-power-play-excerpt",
            "description": "TWITTER",
            "topic": "TWITTER",
            "pubDate": "2021-08-02T09:55:00.000+00:00"
        }
    ],
    "GOOGLE": [
        {
            "title": "Postbiotics see 1,300% increase in Google searches - NutraIngredients.com",
            "link": "https://www.nutraingredients.com/Article/2021/08/02/Postbiotics-see-1-300-increase-in-Google-searches",
            "description": "GOOGLE",
            "topic": "GOOGLE",
            "pubDate": "2021-08-02T09:36:00.000+00:00"
        }
    ]
}
```
