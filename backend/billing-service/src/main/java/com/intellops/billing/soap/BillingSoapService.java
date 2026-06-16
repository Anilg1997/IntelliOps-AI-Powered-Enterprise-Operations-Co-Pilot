package com.intellops.billing.soap;

import com.intellops.billing.entity.Invoice;
import com.intellops.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.util.Optional;

@Endpoint
@RequiredArgsConstructor
@Slf4j
public class BillingSoapService {

    private static final String NAMESPACE_URI = "http://intellops.com/billing/soap";
    private final BillingService billingService;

    @PayloadRoot(localPart = "GetInvoiceRequest", namespace = NAMESPACE_URI)
    @ResponsePayload
    public Element getInvoice(@RequestPayload Element request) throws Exception {
        String invoiceNumber = request.getElementsByTagName("invoiceNumber").item(0).getTextContent();
        log.info("SOAP getInvoice: {}", invoiceNumber);

        Optional<Invoice> invoiceOpt = billingService.getInvoice(invoiceNumber);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document response = builder.newDocument();

        Element root = response.createElementNS(NAMESPACE_URI, "GetInvoiceResponse");
        response.appendChild(root);

        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            addElement(response, root, "invoiceNumber", invoice.getInvoiceNumber());
            addElement(response, root, "orderNumber", invoice.getOrderNumber());
            addElement(response, root, "customerName", invoice.getCustomerName());
            addElement(response, root, "totalAmount", invoice.getTotalAmount().toString());
            addElement(response, root, "status", invoice.getStatus());
            addElement(response, root, "paymentStatus", invoice.getPaymentStatus());
        } else {
            addElement(response, root, "error", "Invoice not found: " + invoiceNumber);
        }

        return root;
    }

    @PayloadRoot(localPart = "CreateInvoiceRequest", namespace = NAMESPACE_URI)
    @ResponsePayload
    public Element createInvoice(@RequestPayload Element request) throws Exception {
        String orderNumber = request.getElementsByTagName("orderNumber").item(0).getTextContent();
        String customerName = request.getElementsByTagName("customerName").item(0).getTextContent();
        String customerEmail = request.getElementsByTagName("customerEmail").item(0).getTextContent();
        BigDecimal totalAmount = new BigDecimal(request.getElementsByTagName("totalAmount").item(0).getTextContent());
        BigDecimal taxAmount = new BigDecimal(request.getElementsByTagName("taxAmount").item(0).getTextContent());

        log.info("SOAP createInvoice for order: {}", orderNumber);

        Invoice invoice = billingService.createInvoice(orderNumber, customerName, customerEmail, totalAmount, taxAmount);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document response = builder.newDocument();

        Element root = response.createElementNS(NAMESPACE_URI, "CreateInvoiceResponse");
        response.appendChild(root);

        addElement(response, root, "invoiceNumber", invoice.getInvoiceNumber());
        addElement(response, root, "status", invoice.getStatus());
        addElement(response, root, "message", "Invoice created successfully");

        return root;
    }

    private void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }
}
