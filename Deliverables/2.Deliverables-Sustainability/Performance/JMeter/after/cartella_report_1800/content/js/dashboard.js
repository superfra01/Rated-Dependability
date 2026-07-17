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

    var data = {"OkPercent": 91.5137048316734, "KoPercent": 8.486295168326611};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.01522759074820973, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.06218905472636816, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.02767412935323383, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [3.6101083032490973E-4, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [8.351893095768374E-4, 500, 1500, "2. Login"], "isController": false}, {"data": [0.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.09375, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [3.0543677458766036E-4, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.0, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.009340338587273789, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.00819672131147541, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.010561423012784881, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 12149, 1031, 8.486295168326611, 24128.08543913085, 3, 154705, 18434.0, 51115.0, 59310.0, 98549.5, 57.6186140041356, 10080.079329191803, 12.729340181442433], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 1608, 0, 0.0, 11382.515547263685, 6, 46082, 10062.0, 27733.60000000001, 41253.1, 41513.73, 11.536060951725029, 32.85853377284075, 2.106683005832598], "isController": false}, {"data": ["2. Login-0", 1608, 0, 0.0, 12645.851368159209, 14, 65481, 7578.0, 31034.90000000024, 57042.55, 61726.24000000001, 16.698339512134336, 2.6163259077385588, 5.018085324568782], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 183, 0, 0.0, 50769.59016393441, 2103, 84241, 50380.0, 79618.8, 81960.2, 83159.92, 1.3211279400511124, 0.1898938111833841, 0.4917921881091266], "isController": false}, {"data": ["5. Ricerca Film", 1385, 3, 0.21660649819494585, 39654.277256317706, 925, 80154, 44785.0, 57693.00000000001, 61337.6, 72030.08000000007, 7.730692804036705, 4543.717137578423, 1.447411752885753], "isController": false}, {"data": ["2. Login", 1796, 188, 10.46770601336303, 24695.44097995547, 1117, 111565, 21038.0, 45833.19999999999, 98410.6, 99803.96999999999, 12.613776828857176, 37.33060772277784, 5.456195920889988], "isController": false}, {"data": ["7. Logout-1", 16, 0, 0.0, 30678.0625, 26745, 35791, 29976.5, 35743.4, 35791.0, 35791.0, 0.14651746305012728, 0.42409937546931376, 0.0241811047416714], "isController": false}, {"data": ["7. Logout-0", 16, 0, 0.0, 28694.5625, 377, 42714, 32694.5, 40278.0, 42714.0, 42714.0, 0.1678345151680443, 0.0219627197583183, 0.028682656400788823], "isController": false}, {"data": ["4. Dettagli Film", 1637, 432, 26.389737324373854, 26617.5326817349, 880, 75599, 28306.0, 44517.8, 47163.1, 53983.67999999999, 8.41035547495132, 998.7102507388603, 1.1001122740070182], "isController": false}, {"data": ["6. Aggiungi Recensione", 189, 6, 3.1746031746031744, 86801.51851851851, 11217, 154705, 80922.0, 119899.0, 124141.5, 133110.39999999988, 1.0447241430774588, 168.07345129319162, 0.5585832345267511], "isController": false}, {"data": ["7. Logout", 16, 0, 0.0, 59373.3125, 27124, 77693, 64382.5, 71089.90000000001, 77693.0, 77693.0, 0.13105945184384266, 0.39650603692600056, 0.0440277846037909], "isController": false}, {"data": ["3. Visualizza Catalogo", 1713, 30, 1.7513134851138354, 29684.134851138388, 114, 83596, 28292.0, 55867.600000000006, 59160.6, 68052.09999999996, 8.375831837940122, 5089.300076712755, 1.3821914101272754], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 183, 0, 0.0, 37831.4262295082, 410, 70464, 31017.0, 57168.0, 59159.99999999999, 61502.039999999964, 1.0240053718314588, 169.9365636016451, 0.1821867480834872], "isController": false}, {"data": ["1. Home Page", 1799, 372, 20.67815453029461, 14607.603112840485, 3, 41423, 10425.0, 30186.0, 32645.0, 40607.0, 40.12042818911686, 113.43733883669714, 3.6050924119090095], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 444, 43.06498545101843, 3.654621779570335], "isController": false}, {"data": ["404", 9, 0.8729388942774006, 0.07408017120750679], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 1, 0.09699321047526673, 0.008231130134167422], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 451, 43.743937924345296, 3.7122396905095068], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 125, 12.124151309408342, 1.0288912667709276], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.TruncatedChunkException/Non HTTP response message: Truncated chunk (expected size: 8,192; actual size: 7,805)", 1, 0.09699321047526673, 0.008231130134167422], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 12149, 1031, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 451, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 444, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 125, "404", 9, "Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 1], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["5. Ricerca Film", 1385, 3, "404", 1, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 1, "Non HTTP response code: org.apache.http.TruncatedChunkException/Non HTTP response message: Truncated chunk (expected size: 8,192; actual size: 7,805)", 1, "", "", "", ""], "isController": false}, {"data": ["2. Login", 1796, 188, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 188, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 1637, 432, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 431, "404", 1, "", "", "", "", "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione", 189, 6, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 5, "404", 1, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["3. Visualizza Catalogo", 1713, 30, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 14, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 9, "404", 6, "Non HTTP response code: org.apache.http.ConnectionClosedException/Non HTTP response message: Premature end of chunk coded message body: closing chunk expected", 1, "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["1. Home Page", 1799, 372, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 247, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 125, "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
