package com.intellops.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableWs
public class SoapConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet() {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setTransformSchemaLocations(true);
        ServletRegistrationBean<MessageDispatcherServlet> bean =
                new ServletRegistrationBean<>(servlet, "/soap/*");
        bean.setName("BillingSoapService");
        return bean;
    }

    @Bean
    public XsdSchema billingSchema() throws IOException {
        InputStream is = getClass().getResourceAsStream("/xsd/billing.xsd");
        if (is != null) {
            return new SimpleXsdSchema(new org.springframework.core.io.InputStreamResource(is));
        }
        // Return a minimal schema if file doesn't exist
        return new SimpleXsdSchema(new org.springframework.core.io.ByteArrayResource(
                "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' targetNamespace='http://intellops.com/billing/soap' xmlns:tns='http://intellops.com/billing/soap' elementFormDefault='qualified'><xs:element name='GetInvoiceRequest'><xs:complexType><xs:sequence><xs:element name='invoiceNumber' type='xs:string'/></xs:sequence></xs:complexType></xs:element><xs:element name='GetInvoiceResponse'><xs:complexType><xs:sequence><xs:element name='invoiceNumber' type='xs:string'/><xs:element name='orderNumber' type='xs:string'/><xs:element name='customerName' type='xs:string'/><xs:element name='totalAmount' type='xs:decimal'/><xs:element name='status' type='xs:string'/><xs:element name='paymentStatus' type='xs:string'/></xs:sequence></xs:complexType></xs:element><xs:element name='CreateInvoiceRequest'><xs:complexType><xs:sequence><xs:element name='orderNumber' type='xs:string'/><xs:element name='customerName' type='xs:string'/><xs:element name='customerEmail' type='xs:string'/><xs:element name='totalAmount' type='xs:decimal'/><xs:element name='taxAmount' type='xs:decimal'/></xs:sequence></xs:complexType></xs:element><xs:element name='CreateInvoiceResponse'><xs:complexType><xs:sequence><xs:element name='invoiceNumber' type='xs:string'/><xs:element name='status' type='xs:string'/><xs:element name='message' type='xs:string'/></xs:sequence></xs:complexType></xs:element></xs:schema>".getBytes()));
    }
}
