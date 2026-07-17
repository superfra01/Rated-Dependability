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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.6902564102564103, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.9766666666666667, 500, 1500, "2. Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.11333333333333333, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.87, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.44333333333333336, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.56, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.9833333333333333, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.02, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.61, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.39666666666666667, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1950, 0, 0.0, 1063.5425641025631, 4, 10776, 344.5, 3428.0, 3767.7999999999993, 9700.93, 111.87607573149742, 14748.450778560671, 29.27328734581182], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 150, 0, 0.0, 179.50666666666663, 31, 467, 171.5, 281.5, 329.9499999999999, 452.72000000000025, 10.317787866281469, 29.387693028614663, 1.884205401361948], "isController": false}, {"data": ["2. Login-0", 150, 0, 0.0, 221.48, 52, 725, 200.5, 362.20000000000005, 492.7999999999997, 707.6600000000003, 10.318497626745545, 1.531651991470042, 3.1601742536286714], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 150, 0, 0.0, 152.25333333333327, 28, 397, 150.0, 253.8, 293.9, 397.0, 14.091122592766558, 2.025507133630813, 5.245548819868483], "isController": false}, {"data": ["5. Ricerca Film", 150, 0, 0.0, 2466.6733333333323, 464, 3913, 2636.0, 3687.3, 3739.8, 3867.6100000000006, 11.641443538998837, 6857.09006354288, 2.1827706635622817], "isController": false}, {"data": ["2. Login", 150, 0, 0.0, 401.66666666666674, 105, 881, 389.5, 607.2, 689.2499999999998, 862.6400000000003, 10.247301543926767, 30.708013816778248, 5.009702913649406], "isController": false}, {"data": ["7. Logout-1", 150, 0, 0.0, 121.66666666666666, 4, 355, 115.5, 207.50000000000003, 246.69999999999993, 338.1700000000003, 15.71503404924044, 45.487657150340496, 2.593594486642221], "isController": false}, {"data": ["7. Logout-0", 150, 0, 0.0, 120.29333333333338, 4, 327, 113.5, 213.70000000000002, 231.39999999999986, 318.33000000000015, 15.394088669950738, 2.014460822044335, 2.6308257004310347], "isController": false}, {"data": ["4. Dettagli Film", 150, 0, 0.0, 1185.3733333333328, 232, 3174, 1089.5, 2119.8, 2851.8, 3138.8100000000004, 9.666817039376168, 1575.1361535936394, 1.7199507999291102], "isController": false}, {"data": ["6. Aggiungi Recensione", 150, 0, 0.0, 929.6333333333336, 163, 2626, 885.5, 1526.5000000000002, 1765.25, 2379.6700000000046, 13.852973771702992, 2259.2306260389732, 7.621660856113779], "isController": false}, {"data": ["7. Logout", 150, 0, 0.0, 242.3133333333333, 8, 614, 241.5, 403.0, 446.49999999999966, 611.45, 15.387771850636028, 46.554020696553145, 5.169329606073041], "isController": false}, {"data": ["3. Visualizza Catalogo", 150, 0, 0.0, 3005.32, 695, 4118, 3468.5, 3886.2, 3984.7, 4114.9400000000005, 9.378516943853944, 5821.1704877610355, 1.6210913076778792], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 150, 0, 0.0, 776.9066666666669, 135, 2331, 718.5, 1313.8000000000002, 1578.1499999999999, 2132.6100000000033, 14.161631419939576, 2307.53282621908, 2.5196824549188066], "isController": false}, {"data": ["1. Home Page", 150, 0, 0.0, 4022.9666666666662, 95, 10776, 2985.5, 9934.0, 10459.449999999999, 10760.7, 10.228435049437437, 29.606524889191956, 1.1586899079440847], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1950, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
