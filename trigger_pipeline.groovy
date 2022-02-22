pipeline{
    agent {
        any
    }
    triggers {
        cron('* * * * *')
    }
    environment {
        SERVICE_NAME = 'AUTHSERVICE'
        METRICS_ENDPOINT = '/actuator/metrics/tomcat.threads.busy?tag=name:http-nio-auto-1'
        SHUTDOWN_ENDPOINT = '/actuator/shutdown'
    }
    /*The first stage of our pipeline is responsible for fetching a list of services
    registered on the service discovery server
    */
    stage('Calculate') {
        steps {
            script {
                def response = httpRequest "http://localhost:8761/eureka/apps/${env.SERVICE_NAME}"
                def app = printXml(response.content)
                def index = 0
                env["INSTANCE_COUNT"] = app.instance.size()
                app.instance.each {
                    if (it.status == 'UP') {
                        def address = "http://${it.ipAddr}:${it.port}"
                        env["INSTANCE_${index++}"] = address
                    }
                }
            }
        }
    }
    @NonCPS
    def printXml(String text) {
        return new XmlSlurper(false, false).parseText(text)
    }
    /*
Spring Boot Actuator exposes endpoint with metrics,
which allows us to find the metric by name and optionally by tag
*/
stage('Metrics') {
    steps {
        script {
            def count = env.INSTANCE_COUNT
            for(def i=0; i<count; i++) {
                def ip = env["INSTANCE_${i}"] + env.METRICS_ENDPOINT
                if (ip == null)
                    break;
                def response = httpRequest ip
                def objRes = printJson(response.content)
                env.SCALE_TYPE = returnScaleType(objRes)
                if (env.SCALE_TYPE != "NONE")
                    break
            }
        }
    }
}

@NonCPS
def printJson(String text) {
    return new JsonSlurper().parseText(text)
}

def returnScaleType(objRes) {
    def value = objRes.measurements[0].value
    if (value.toInteger() > 100)
        return "UP"
    else if (value.toInteger() < 20)
        return "DOWN"
    else
        return "NONE"
}
stage('Scaling') {
 steps {
  script {
   if (env.SCALE_TYPE == 'DOWN') {
    def ip = env["INSTANCE_0"] + env.SHUTDOWN_ENDPOINT
    httpRequest url: ip, contentType: 'APPLICATION_JSON', httpMode: 'POST'
   } else if (env.SCALE_TYPE == 'UP') {
    build job: 'scaling_pipeline'
   }
   currentBuild.description = env.SCALE_TYPE
  }
 }
}
}

