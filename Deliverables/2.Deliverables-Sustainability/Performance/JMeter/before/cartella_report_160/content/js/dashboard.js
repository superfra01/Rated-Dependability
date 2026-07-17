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

    var data = {"OkPercent": 57.083333333333336, "KoPercent": 42.916666666666664};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.2503472222222222, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.928125, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.328125, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.2875, 500, 1500, "2. Login"], "isController": false}, {"data": [0.0, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.0, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.0, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.0, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.0, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.709375, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1440, 618, 42.916666666666664, 27557.07499999996, 7, 231419, 600.0, 150012.0, 188612.95, 207961.93999999997, 5.842187899368314, 4325.741480364518, 0.8437725357834008], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 160, 0, 0.0, 403.18750000000006, 7, 11216, 145.5, 426.20000000000016, 2133.8999999999965, 9872.77999999997, 3.0159657687885244, 8.51962468897853, 0.5507671862924356], "isController": false}, {"data": ["2. Login-0", 160, 0, 0.0, 3595.912499999999, 21, 47009, 1514.0, 6032.3, 28172.799999999785, 37926.099999999795, 2.980070776680946, 0.44235425591357797, 0.9127558088098343], "isController": false}, {"data": ["2. Login", 160, 0, 0.0, 3999.825, 77, 47190, 1679.5, 6754.7000000000035, 29127.949999999888, 39630.26999999983, 2.970058101761616, 8.83081081425999, 1.4520727176959773], "isController": false}, {"data": ["4. Dettagli Film", 160, 137, 85.625, 145044.73124999998, 111, 227106, 185121.0, 206304.1, 218799.1, 227083.43, 0.7042966497488743, 60.20140253387227, 0.0258565939223601], "isController": false}, {"data": ["5. Ricerca Film", 160, 157, 98.125, 39630.66249999999, 112, 174661, 234.0, 157143.7, 163501.25, 173775.88999999998, 0.9116913013253712, 1.866947246193689, 0.0032051647312220082], "isController": false}, {"data": ["6. Aggiungi Recensione", 160, 160, 100.0, 6344.6937500000095, 105, 163232, 530.0, 607.0, 622.95, 162257.83, 0.974445175278326, 1.9907610416818924, 0.0], "isController": false}, {"data": ["7. Logout", 160, 160, 100.0, 209.6187499999998, 55, 604, 151.0, 479.30000000000007, 502.74999999999994, 603.39, 154.58937198067633, 315.8212560386474, 0.0], "isController": false}, {"data": ["3. Visualizza Catalogo", 160, 4, 2.5, 48006.5375, 16795, 231419, 49027.5, 56590.7, 60357.44999999996, 216769.8499999997, 0.6554314787353449, 4301.896423616374, 0.11046004633081265], "isController": false}, {"data": ["1. Home Page", 160, 0, 0.0, 778.5062500000004, 10, 3686, 228.5, 3036.9, 3270.199999999999, 3667.0899999999997, 19.49555257706836, 55.973559156817345, 2.2084805653710244], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["404", 10, 1.6181229773462784, 0.6944444444444444], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 608, 98.38187702265373, 42.22222222222222], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1440, 618, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 608, "404", 10, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 160, 137, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 127, "404", 10, "", "", "", "", "", ""], "isController": false}, {"data": ["5. Ricerca Film", 160, 157, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 157, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione", 160, 160, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 160, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["7. Logout", 160, 160, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 160, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["3. Visualizza Catalogo", 160, 4, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 4, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
