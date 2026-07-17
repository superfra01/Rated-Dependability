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

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.6134615384615385, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9966666666666667, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.995, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.975, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.04666666666666667, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.83, 500, 1500, "2. Login"], "isController": false}, {"data": [0.9933333333333333, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.985, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.3, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.305, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.87, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.005, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.3983333333333333, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.275, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 3900, 0, 0.0, 1653.8987179487192, 3, 21488, 467.0, 4217.6, 5553.399999999998, 15275.579999999947, 130.06503251625813, 17031.340702903537, 34.03374864515591], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 300, 0, 0.0, 201.3333333333334, 11, 626, 176.0, 345.0, 413.6499999999999, 498.97, 11.770244821092279, 33.52496763182674, 2.1494490054143127], "isController": false}, {"data": ["2. Login-0", 300, 0, 0.0, 234.53000000000003, 11, 725, 205.5, 402.90000000000003, 420.95, 549.4500000000005, 11.771630370806356, 1.7473513831665686, 3.6055216303708066], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 300, 0, 0.0, 259.4966666666667, 46, 674, 222.0, 432.50000000000017, 504.39999999999986, 597.7900000000002, 12.762155953545752, 1.8346430052750244, 4.750995049347003], "isController": false}, {"data": ["5. Ricerca Film", 300, 0, 0.0, 3985.7566666666635, 334, 6434, 4038.5, 5785.6, 6095.75, 6333.74, 10.935734334560566, 6441.410664163599, 2.0504501877301062], "isController": false}, {"data": ["2. Login", 300, 0, 0.0, 437.1566666666667, 77, 977, 416.0, 707.0, 766.3499999999999, 969.6700000000003, 11.706403402661255, 35.08079857181879, 5.723333788582355], "isController": false}, {"data": ["7. Logout-1", 300, 0, 0.0, 199.30666666666653, 3, 665, 188.0, 369.7000000000001, 439.95, 533.8500000000001, 13.6332651670075, 39.46191206543968, 2.2500213019768234], "isController": false}, {"data": ["7. Logout-0", 300, 0, 0.0, 203.85333333333332, 4, 586, 184.5, 389.80000000000007, 457.95, 569.97, 13.534241631327259, 1.7710824009744655, 2.3129807475412796], "isController": false}, {"data": ["4. Dettagli Film", 300, 0, 0.0, 1610.346666666667, 350, 4905, 1431.0, 2841.300000000001, 3348.7, 4366.910000000001, 10.913456291607552, 1736.4769416289244, 1.9418989186583724], "isController": false}, {"data": ["6. Aggiungi Recensione", 300, 0, 0.0, 1589.2166666666653, 359, 4823, 1316.5, 2891.7000000000025, 3282.5, 3926.020000000002, 12.594458438287154, 2005.7574140034635, 6.929575986565911], "isController": false}, {"data": ["7. Logout", 300, 0, 0.0, 403.4833333333335, 8, 1010, 366.5, 689.6000000000001, 821.4499999999998, 940.0, 13.531799729364005, 40.938980040595396, 4.54583897158322], "isController": false}, {"data": ["3. Visualizza Catalogo", 300, 0, 0.0, 4019.4400000000014, 808, 6595, 3882.5, 5445.500000000001, 5950.549999999999, 6548.980000000001, 10.765807794444843, 6682.251107981052, 1.8608866988444699], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 300, 0, 0.0, 1329.333333333334, 286, 4475, 1076.5, 2612.9000000000005, 2846.1, 3707.0000000000027, 12.774111134766873, 2032.5320267591017, 2.2729767670853738], "isController": false}, {"data": ["1. Home Page", 300, 0, 0.0, 7027.4299999999985, 67, 21488, 3702.5, 16643.400000000005, 19967.249999999996, 21464.89, 11.627005658476087, 33.65473122238586, 1.3171217347492443], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 3900, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
