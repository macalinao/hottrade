/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hottrade.site;

import static spark.Spark.*;

/**
 *
 * @author simplyianm
 */
public class HotTradeApp {

    public static void main(String[] args) {
        Bloomberg b = new Bloomberg();
        b.connect();

        staticFileLocation("/public");

        get("/api/match", "application/json", (request, response) -> {
            try {
                return QuickJson.toJson(b.getStockData());
            } catch (Exception e) {
                e.printStackTrace();
                return "omg";
            }
        });

        get("/api/stock/:symbol", "application/json", (request, response) -> {
            try {
                return QuickJson.toJson(b.getIndividualStockData(request.params("symbol")));
            } catch (Exception e) {
                e.printStackTrace();
                return "fml";
            }
        });
    }
}
