/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hottrade.site;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simplyianm
 */
public class Bloomberg {

    private Session session = null;

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Session connect() {
        if (session != null) {
            throw new IllegalStateException("Already connected.");
        }

        String serverHost = "10.8.8.1";
        int serverPort = 8194;

        SessionOptions sessionOptions = new SessionOptions();
        sessionOptions.setServerHost(serverHost);
        sessionOptions.setServerPort(serverPort);

        session = new Session(sessionOptions);

        System.out.println("Connecting to " + serverHost + ":" + serverPort);
        try {
            if (!session.start()) {
                System.err.println("Failed to start session.");
            }
        } catch (IOException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (!session.openService("//blp/refdata")) {
                System.err.println("Failed to open //blp/refdata");
                return session;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Connected successfully.");

        return session;
    }

    public List<Map<String, Object>> getStockData(String industry) {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Map<String, Object> s : getStockData()) {
            if (s.get("industry").toString().equalsIgnoreCase(industry)) {
                ret.add(s);
            }
        }
        return ret;
    }

    public List<Map<String, Object>> getStockData() {
        List<Map<String, Object>> ret = new ArrayList<>();

        Service refDataService = session.getService("//blp/refdata");
        Request request = refDataService.createRequest("ReferenceDataRequest");

        Element securities = request.getElement("securities");
        for (String security : SP500.sp500) {
            securities.appendValue(security + " US Equity");
        }

        Element fields = request.getElement("fields");
        fields.appendValue("INDUSTRY_SECTOR_RT");
        fields.appendValue("LONG_COMPANY_NAME_REALTIME");
        fields.appendValue("LOCAL_EXCHANGE_SYMBOL_REALTIME");
        fields.appendValue("LOCAL_EXCHANGE_SYMBOL_REALTIME");
        fields.appendValue("PX_LAST");
        fields.appendValue("CUR_MKT_CAP");

        CorrelationID theCid;
        try {
            theCid = session.sendRequest(request, null);
        } catch (IOException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }

        for (;;) {
            Event event;
            try {
                event = session.nextEvent();
            } catch (InterruptedException ex) {
                Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            MessageIterator msgIter = event.messageIterator();
            while (msgIter.hasNext()) {
                Message msg = msgIter.next();
                if (msg.correlationID() != theCid) {
                    continue;
                }

                // Processing
                Element securityDataArray = msg.getElement("securityData");

                for (int i = 0; i < securityDataArray.numValues(); i++) {

                    Element securityData = securityDataArray.getValueAsElement(i);
                    Element fieldData = securityData.getElement("fieldData");

                    try {
                        String name = securityData.getElementAsString("security");

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", fieldData.getElementAsString("LONG_COMPANY_NAME_REALTIME"));
                        data.put("symbol", fieldData.getElementAsString("LOCAL_EXCHANGE_SYMBOL_REALTIME"));
                        data.put("industry", fieldData.getElementAsString("INDUSTRY_SECTOR_RT"));
                        data.put("last", fieldData.getElementAsFloat64("PX_LAST"));
                        data.put("marketCap", fieldData.getElementAsFloat64("CUR_MKT_CAP"));

                        ret.add(data);
                    } catch (Exception e) {
                        continue;
                    }
                }

            }
            if (event.eventType() == Event.EventType.RESPONSE) {
                return ret;
            }
        }
    }

    public List<Map<String, Object>> getIndividualStockData(String stock) {
        List<Map<String, Object>> ret = new ArrayList<>();

        Service refDataService = session.getService("//blp/refdata");
        Request request = refDataService.createRequest("HistoricalDataRequest");
        request.append("securities", stock + " US Equity");
        request.append("fields", "OPEN");
        request.set("startDate", "19700101");
        request.set("endDate", (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
        request.set("periodicitySelection", "DAILY");

        CorrelationID theCid;
        try {
            theCid = session.sendRequest(request, null);
        } catch (IOException ex) {
            Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }

        for (;;) {
            Event event;
            try {
                event = session.nextEvent();
            } catch (InterruptedException ex) {
                Logger.getLogger(Bloomberg.class.getName()).log(Level.SEVERE, null, ex);
                return ret;
            }

            MessageIterator msgIter = event.messageIterator();
            while (msgIter.hasNext()) {
                Message msg = msgIter.next();
                if (msg.correlationID() != theCid) {
                    continue;
                }

                // Processing
                Element securityData = msg.getElement("securityData");
                Element fieldDataArr = securityData.getElement("fieldData");

                for (int j = 0; j < fieldDataArr.numValues(); ++j) {
                    Element fieldData = fieldDataArr.getValueAsElement(j);

                    try {
                        Map<String, Object> di = new HashMap<>();
                        di.put("date", DATE_FORMAT.format(DATE_FORMAT.parse(fieldData.getElementAsString("date"))));
                        di.put("open", fieldData.getElementAsFloat64("OPEN"));
                        ret.add(di);
                    } catch (Exception ex) {
                    }

                }

            }
            if (event.eventType() == Event.EventType.RESPONSE) {
                return ret;
            }
        }
    }
}
