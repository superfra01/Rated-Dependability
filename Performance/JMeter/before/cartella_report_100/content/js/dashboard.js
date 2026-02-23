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

    var data = {"OkPercent": 99.84615384615384, "KoPercent": 0.15384615384615385};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.7423076923076923, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9625, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.85, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.5775, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.5, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.77, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.5, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.49, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.985, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.5, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.8, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.715, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 2600, 4, 0.15384615384615385, 2082.3707692307657, 2, 36258, 50.5, 4531.000000000001, 13306.599999999977, 34043.75999999997, 6.225397707138137, 2971.457187520951, 1.6272830448899063], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 200, 0, 0.0, 158.84499999999997, 3, 1485, 18.0, 455.9, 630.4999999999999, 1458.7400000000002, 0.5354637651670111, 1.5125753246918408, 0.09778488680296005], "isController": false}, {"data": ["2. Login-0", 200, 0, 0.0, 398.21999999999997, 5, 3601, 34.0, 1676.8000000000002, 2277.0, 3490.190000000005, 0.5360651211909223, 0.07957216642677753, 0.1641647083537708], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 200, 0, 0.0, 1135.6750000000004, 5, 5332, 604.5, 3030.7000000000003, 4166.099999999999, 5285.3600000000015, 0.47941051682850766, 0.06892930702408319, 0.17848210090872266], "isController": false}, {"data": ["5. Ricerca Film", 200, 0, 0.0, 5024.189999999999, 5, 17006, 1684.0, 13904.8, 14476.45, 16543.710000000003, 0.4818510791054917, 651.1775433379872, 0.09034707733227969], "isController": false}, {"data": ["2. Login", 200, 0, 0.0, 557.7700000000001, 8, 4110, 51.0, 2113.0, 2734.199999999999, 3682.4900000000016, 0.5354107269539146, 1.5919005323990416, 0.2617395076229102], "isController": false}, {"data": ["7. Logout-1", 200, 0, 0.0, 33.15000000000001, 3, 334, 8.0, 88.70000000000002, 158.95, 327.3300000000006, 0.47951281497998033, 1.376726246133928, 0.07913834544103192], "isController": false}, {"data": ["7. Logout-0", 200, 0, 0.0, 42.56, 2, 361, 7.5, 140.2000000000001, 251.89999999999998, 356.73000000000025, 0.4795093660166917, 0.06274829594359052, 0.08194740141886821], "isController": false}, {"data": ["4. Dettagli Film", 200, 4, 2.0, 1927.4199999999992, 3, 6457, 98.0, 5569.2, 5881.7, 6389.59, 0.4856313814998725, 176.49927781301977, 0.08469145713696019], "isController": false}, {"data": ["6. Aggiungi Recensione", 200, 0, 0.0, 1801.665, 29, 8556, 937.5, 4792.6, 5883.65, 7359.110000000001, 0.47928873551649354, 182.8329235976311, 0.2637304989395737], "isController": false}, {"data": ["7. Logout", 200, 0, 0.0, 76.13500000000002, 6, 512, 17.0, 266.0, 333.34999999999985, 508.99, 0.4795036178547967, 1.4394473840680126, 0.16108324662309575], "isController": false}, {"data": ["3. Visualizza Catalogo", 200, 0, 0.0, 14391.665000000006, 5, 36258, 8391.0, 34413.1, 34951.75, 35525.86, 0.4900567485714845, 1819.310836448767, 0.08470707470425076], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 200, 0, 0.0, 665.435, 21, 5145, 81.0, 3285.1000000000004, 3826.649999999999, 4456.400000000004, 0.4794116660034805, 182.81088807190815, 0.08531561317950612], "isController": false}, {"data": ["1. Home Page", 200, 0, 0.0, 858.0899999999999, 6, 4801, 69.0, 3455.5, 3700.1, 4779.210000000001, 0.5381132136390175, 1.544973484471398, 0.06095813748254496], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 4, 100.0, 0.15384615384615385], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 2600, 4, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 4, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 200, 4, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 4, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
