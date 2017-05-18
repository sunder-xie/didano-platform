package cn.didano.config;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.valves.RemoteIpValve;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfiguration {


@Bean
public EmbeddedServletContainerFactory servletContainer() {
    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
    //tomcat.addAdditionalTomcatConnectors(createConnector());
    //tomcat.addContextValves(createRemoteIpValves());
    return tomcat;
}

//private RemoteIpValve createRemoteIpValves(){
//    RemoteIpValve remoteIpValve = new RemoteIpValve();
//    remoteIpValve.setRemoteIpHeader("x-forwarded-for");
//    remoteIpValve.setProtocolHeader("x-forwarded-protocol");
//    return remoteIpValve;
//}

//private Connector createConnector() {
//    Connector connector = new Connector("AJP/1.3");
//    connector.setScheme("ajp");
//    connector.setProtocol("AJP/1.3");
//    connector.setRedirectPort(8443);
//    //connector.setSecure(true);
//    connector.setPort(8009);
//    return connector;
//}
  
}