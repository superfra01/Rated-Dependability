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

    var data = {"OkPercent": 99.38461538461539, "KoPercent": 0.6153846153846154};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.48333333333333334, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9433333333333334, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.38333333333333336, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.38333333333333336, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.03, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.3566666666666667, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.0, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.17666666666666667, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.9933333333333333, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.0, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.2733333333333333, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.7433333333333333, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1950, 12, 0.6153846153846154, 8058.821538461533, 4, 75364, 1044.5, 32307.100000000002, 52025.349999999955, 63694.57, 19.131151400988934, 16585.84234245006, 4.989840791416491], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 150, 0, 0.0, 324.2800000000001, 7, 12490, 149.5, 547.0000000000006, 998.4999999999985, 6885.6100000001, 2.715423606082549, 7.6705766880883415, 0.4958830218139029], "isController": false}, {"data": ["2. Login-0", 150, 0, 0.0, 2077.38, 55, 36117, 1173.5, 4086.5000000000014, 5455.0, 22056.30000000025, 3.448196593181766, 0.5118416818004184, 1.0560551050550562], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 150, 0, 0.0, 3330.286666666667, 71, 13242, 1892.5, 8714.8, 11355.849999999993, 12819.720000000008, 5.639309748486785, 0.8103203480394, 2.0989907397834506], "isController": false}, {"data": ["5. Ricerca Film", 150, 1, 0.6666666666666666, 5771.026666666667, 456, 17287, 5409.0, 9708.800000000001, 11005.949999999993, 17256.4, 4.215377697841727, 11222.600127295624, 0.7851140962230215], "isController": false}, {"data": ["2. Login", 150, 0, 0.0, 2402.3266666666677, 86, 48608, 1359.5, 4852.200000000001, 5809.249999999999, 28524.71000000036, 2.6789541363051868, 7.965214338655523, 1.3096876562723248], "isController": false}, {"data": ["7. Logout-1", 150, 0, 0.0, 81.00000000000007, 4, 314, 59.0, 186.20000000000005, 265.45, 313.49, 9.021471101220905, 25.90148929452096, 1.488895132916341], "isController": false}, {"data": ["7. Logout-0", 150, 0, 0.0, 83.55999999999999, 4, 435, 59.0, 186.30000000000004, 239.14999999999992, 368.7000000000012, 8.978810008380222, 1.1749614659403806, 1.534464601041542], "isController": false}, {"data": ["4. Dettagli Film", 150, 7, 4.666666666666667, 39962.52666666666, 4061, 75364, 37341.0, 64710.8, 68299.24999999999, 74339.41000000002, 1.9775091295004812, 1389.9456303433947, 0.342369142453166], "isController": false}, {"data": ["6. Aggiungi Recensione", 150, 0, 0.0, 5890.946666666667, 441, 20169, 4736.0, 14152.6, 14973.2, 19052.61000000002, 5.6114623470876515, 4174.276274795649, 3.086742686394074], "isController": false}, {"data": ["7. Logout", 150, 0, 0.0, 164.87333333333333, 9, 570, 117.5, 393.30000000000007, 459.45, 559.8000000000002, 8.976123511459518, 26.945902025611876, 3.015416492130932], "isController": false}, {"data": ["3. Visualizza Catalogo", 150, 4, 2.6666666666666665, 41433.500000000015, 17836, 71865, 44496.0, 55858.1, 56657.549999999996, 69413.94000000005, 1.8242626938279112, 11680.10708041958, 0.30691794618425056], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 150, 0, 0.0, 2560.2200000000007, 115, 18526, 2118.5, 4474.800000000002, 8824.099999999995, 14969.770000000062, 5.9789540816326525, 4446.788320736009, 1.0634831019810267], "isController": false}, {"data": ["1. Home Page", 150, 0, 0.0, 682.7533333333334, 9, 3772, 243.0, 2339.500000000001, 3611.8, 3754.1500000000005, 18.707907208780245, 53.7121554627089, 2.1192551134946367], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: org.apache.http.TruncatedChunkException/Non HTTP response message: Truncated chunk (expected size: 8,192; actual size: 612)", 1, 8.333333333333334, 0.05128205128205128], "isController": false}, {"data": ["404", 3, 25.0, 0.15384615384615385], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 8, 66.66666666666667, 0.41025641025641024], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1950, 12, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 8, "404", 3, "Non HTTP response code: org.apache.http.TruncatedChunkException/Non HTTP response message: Truncated chunk (expected size: 8,192; actual size: 612)", 1, "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["5. Ricerca Film", 150, 1, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 150, 7, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 4, "404", 3, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["3. Visualizza Catalogo", 150, 4, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 3, "Non HTTP response code: org.apache.http.TruncatedChunkException/Non HTTP response message: Truncated chunk (expected size: 8,192; actual size: 612)", 1, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
