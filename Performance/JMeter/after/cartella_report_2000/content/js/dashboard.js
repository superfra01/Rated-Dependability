/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 90.88253382533826, "KoPercent": 9.117466174661747};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.02890528905289053, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.07976554536187563, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.046636085626911315, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.0, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.004752376188094047, 500, 1500, "2. Login"], "isController": false}, {"data": [0.024390243902439025, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [3.5335689045936394E-4, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.0, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.007038440714672442, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.052, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 13008, 1186, 9.117466174661747, 32254.7579182042, 2, 646579, 25128.5, 63409.1, 81465.2999999999, 115059.59999999998, 17.392071686140493, 2731.0783881648877, 3.868888921478968], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 1962, 0, 0.0, 16541.320591233452, 2, 61706, 7120.0, 40001.4, 41707.79999999999, 45273.689999999944, 10.968308185980467, 31.241270504433167, 2.00300159255698], "isController": false}, {"data": ["2. Login-0", 1962, 0, 0.0, 20869.87512742102, 6, 84216, 5976.0, 63963.7, 65769.7, 79124.45999999999, 14.818507273304029, 2.4745528178388545, 4.345062173343304], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 90, 0, 0.0, 39681.56666666668, 3928, 63430, 41150.0, 55201.6, 60719.4, 63430.0, 0.622346384167508, 0.08950281006679851, 0.2317186830113267], "isController": false}, {"data": ["5. Ricerca Film", 1362, 37, 2.7165932452276063, 43816.35022026433, 20, 80197, 46713.5, 55555.5, 59935.4, 66694.9199999999, 7.267294506843102, 4164.832140570392, 1.3256009390923886], "isController": false}, {"data": ["2. Login", 1999, 37, 1.8509254627313656, 36721.059029514756, 8, 145922, 15142.0, 103904.0, 105051.0, 110493.0, 11.140151916228733, 33.499079911711924, 5.20276772727526], "isController": false}, {"data": ["7. Logout-1", 41, 5, 12.195121951219512, 102961.80487804876, 488, 608143, 33483.0, 603721.2, 604081.0, 608143.0, 0.05738639942333667, 0.16353702297555478, 0.008315997858507533], "isController": false}, {"data": ["7. Logout-0", 41, 0, 0.0, 55213.634146341465, 13951, 79053, 63913.0, 78561.4, 78893.5, 79053.0, 0.329359596413997, 0.05604384900870795, 0.04713990603210051], "isController": false}, {"data": ["4. Dettagli Film", 1415, 337, 23.81625441696113, 32969.53710247355, 10, 81966, 31876.0, 51993.80000000001, 56232.8, 64661.79999999993, 7.95556130280047, 965.1226305938419, 1.0784173598499969], "isController": false}, {"data": ["6. Aggiungi Recensione", 156, 69, 44.23076923076923, 163374.7179487179, 13180, 635059, 81245.0, 598256.3, 598748.2, 634988.89, 0.21009818049588558, 17.873726816389677, 0.07245667356331902], "isController": false}, {"data": ["7. Logout", 43, 7, 16.27906976744186, 178842.6046511628, 34266, 646579, 106941.0, 635597.8, 636689.8, 646579.0, 0.05903244450607005, 0.17559041882832954, 0.016212739630539267], "isController": false}, {"data": ["3. Visualizza Catalogo", 1847, 156, 8.44612885760693, 48459.571737953425, 13, 608167, 28702.0, 47369.6, 83424.2, 607680.88, 2.4767345187330703, 1402.9247689375654, 0.39403499376458284], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 90, 3, 3.3333333333333335, 54210.73333333335, 4164, 596690, 41573.5, 50297.90000000001, 67939.90000000004, 596690.0, 0.12205475104899279, 17.8197406968004, 0.021000674776300764], "isController": false}, {"data": ["1. Home Page", 2000, 535, 26.75, 14408.473499999991, 7, 90159, 4099.0, 30693.0, 44359.399999999994, 80879.42000000001, 21.068821304792102, 59.155756693301164, 1.7482595178400244], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 442, 37.268128161888704, 3.3979089790897907], "isController": false}, {"data": ["404", 24, 2.0236087689713322, 0.18450184501845018], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 3, 0.25295109612141653, 0.023062730627306273], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 454, 38.27993254637437, 3.490159901599016], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 263, 22.17537942664418, 2.0218327183271834], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 13008, 1186, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 454, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 442, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 263, "404", 24, "Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 3], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["5. Ricerca Film", 1362, 37, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 22, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 15, "", "", "", "", "", ""], "isController": false}, {"data": ["2. Login", 1999, 37, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 37, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["7. Logout-1", 41, 5, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 5, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 1415, 337, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 300, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 37, "", "", "", "", "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione", 156, 69, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 32, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 22, "404", 15, "", "", "", ""], "isController": false}, {"data": ["7. Logout", 43, 7, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 7, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["3. Visualizza Catalogo", 1847, 156, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 107, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 37, "404", 9, "Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 3, "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 90, 3, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 3, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["1. Home Page", 2000, 535, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 405, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 130, "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
