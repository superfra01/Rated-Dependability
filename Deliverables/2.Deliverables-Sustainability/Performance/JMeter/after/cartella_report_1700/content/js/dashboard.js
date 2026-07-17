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

    var data = {"OkPercent": 96.28021148036254, "KoPercent": 3.7197885196374623};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.11695147280966767, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.1691616766467066, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.12904191616766467, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.04631083202511774, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.007352941176470588, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.06647058823529411, 500, 1500, "2. Login"], "isController": false}, {"data": [0.5203180212014135, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.28209658421672557, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.003235294117647059, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [8.823529411764706E-4, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.185, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.03441176470588235, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.032574568288854, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.006470588235294118, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 21184, 788, 3.7197885196374623, 19788.02374433534, 1, 132596, 14335.0, 44057.9, 54661.350000000006, 99868.63000000006, 110.53309888183331, 14286.137128364806, 27.268561214799664], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 1670, 0, 0.0, 8609.42754491016, 5, 15316, 12878.5, 13366.8, 14036.45, 14504.869999999999, 23.275261324041814, 66.29545949477352, 4.250462761324042], "isController": false}, {"data": ["2. Login-0", 1670, 0, 0.0, 10110.338922155679, 7, 39099, 11813.0, 14679.000000000004, 21666.799999999992, 32147.139999999992, 28.597849167751217, 4.576110735324337, 8.52640939661963], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 1274, 0, 0.0, 29501.00000000001, 27, 105686, 19186.0, 75241.5, 99676.75, 104711.25, 6.9324278733661995, 0.9964844302648905, 2.5806525185145888], "isController": false}, {"data": ["5. Ricerca Film", 1700, 0, 0.0, 27048.068823529404, 171, 49616, 25553.0, 44397.3, 45221.7, 46867.74, 9.499064062805576, 5595.173004323471, 1.7810745117760456], "isController": false}, {"data": ["2. Login", 1700, 30, 1.7647058823529411, 18397.018823529415, 3, 49703, 24654.0, 27366.9, 32266.199999999997, 44144.94, 23.53983771358941, 70.6334412386801, 11.117434798111274], "isController": false}, {"data": ["7. Logout-1", 1698, 0, 0.0, 7451.554770318003, 4, 46035, 846.0, 22806.1, 24905.25, 44712.14, 9.546136850449479, 27.631591430402594, 1.5754854762948847], "isController": false}, {"data": ["7. Logout-0", 1698, 0, 0.0, 11695.494110718479, 3, 45991, 4834.5, 39241.8, 44602.2, 45490.55, 9.490699338222143, 1.241946983712663, 1.6219456876844482], "isController": false}, {"data": ["4. Dettagli Film", 1700, 15, 0.8823529411764706, 25247.141764705888, 506, 52773, 27863.5, 40559.0, 43534.85, 47300.69, 11.701702941945786, 1823.3899577985828, 2.0560661068778483], "isController": false}, {"data": ["6. Aggiungi Recensione", 1700, 426, 25.058823529411764, 37732.48705882364, 1, 132596, 28228.5, 93312.50000000004, 112708.44999999994, 130583.54, 9.211795433116947, 1097.64039605979, 3.858624678941836], "isController": false}, {"data": ["7. Logout", 1700, 2, 0.11764705882352941, 19126.105294117657, 10, 70444, 5242.5, 61499.20000000001, 67379.6, 69444.0, 9.501187648456057, 28.733822656140838, 3.1880501606818497], "isController": false}, {"data": ["3. Visualizza Catalogo", 1700, 21, 1.2352941176470589, 22373.308823529413, 2, 47140, 18096.0, 42801.8, 44615.95, 45672.62, 14.567765818879826, 8930.866126650657, 2.4829638815812025], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 1274, 0, 0.0, 20237.570643642084, 428, 50721, 15672.0, 44746.0, 46500.5, 48226.0, 6.9136928019449515, 1093.8691502490205, 1.2300992639277808], "isController": false}, {"data": ["1. Home Page", 1700, 294, 17.294117647058822, 21869.314705882345, 10, 41738, 31325.5, 40607.6, 40778.95, 41241.84, 36.96375377791307, 104.91465466748929, 3.4631436041834274], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 35, 4.441624365482234, 0.1652190332326284], "isController": false}, {"data": ["404", 30, 3.8071065989847717, 0.14161631419939577], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 413, 52.411167512690355, 1.949584592145015], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 310, 39.34010152284264, 1.463368580060423], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 21184, 788, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 413, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 310, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 35, "404", 30, "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["2. Login", 1700, 30, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 30, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 1700, 15, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 15, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione", 1700, 426, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 396, "404", 30, "", "", "", "", "", ""], "isController": false}, {"data": ["7. Logout", 1700, 2, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 2, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["3. Visualizza Catalogo", 1700, 21, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 21, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["1. Home Page", 1700, 294, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 259, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 35, "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
