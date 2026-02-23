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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.5861834319526628, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.7946153846153846, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.7453846153846154, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.7880769230769231, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.2676923076923077, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.605, 500, 1500, "2. Login"], "isController": false}, {"data": [0.9457692307692308, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.9361538461538461, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.39615384615384613, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.39153846153846156, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.8311538461538461, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.23653846153846153, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.5107692307692308, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.17153846153846153, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 16900, 0, 0.0, 3462.620887573975, 2, 60243, 618.0, 8735.9, 13427.949999999999, 56746.30000000011, 240.61392143741904, 31577.7988998409, 62.9599383159873], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 1300, 0, 0.0, 438.842307692308, 3, 1887, 326.0, 1058.3000000000006, 1318.8500000000001, 1738.7300000000002, 20.21050013214558, 57.56597805217419, 3.690784692100804], "isController": false}, {"data": ["2. Login-0", 1300, 0, 0.0, 639.2423076923073, 15, 7245, 390.0, 1471.4000000000015, 2761.0000000000036, 4880.27, 20.184141475305477, 2.9960835002406574, 6.182970212865062], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 1300, 0, 0.0, 484.8392307692314, 10, 5393, 232.5, 1085.4000000000005, 1681.4000000000015, 3575.970000000002, 19.60873041012414, 2.818401473671509, 7.299302758797532], "isController": false}, {"data": ["5. Ricerca Film", 1300, 0, 0.0, 4622.150000000002, 34, 11885, 1636.5, 10174.9, 10488.85, 11224.96, 19.269821976490817, 11350.389576971822, 3.6130916205920287], "isController": false}, {"data": ["2. Login", 1300, 0, 0.0, 1078.313076923076, 66, 8436, 736.5, 2517.800000000002, 3513.250000000002, 5965.700000000001, 20.127889513369563, 60.31841014832706, 9.841437228079954], "isController": false}, {"data": ["7. Logout-1", 1300, 0, 0.0, 191.27461538461512, 2, 1277, 85.5, 531.9000000000001, 621.95, 993.99, 20.42483660130719, 59.120327818627445, 3.370895884395425], "isController": false}, {"data": ["7. Logout-0", 1300, 0, 0.0, 202.2946153846153, 2, 1591, 89.0, 558.8000000000002, 683.5500000000004, 1001.9300000000001, 20.270691698372108, 2.652610046466662, 3.464229538296014], "isController": false}, {"data": ["4. Dettagli Film", 1300, 0, 0.0, 2118.0176923076924, 164, 9329, 1279.0, 4336.300000000001, 6766.300000000001, 8792.93, 19.401537198716515, 3111.7312548970226, 3.4517596634579513], "isController": false}, {"data": ["6. Aggiungi Recensione", 1300, 0, 0.0, 1849.3292307692295, 205, 7447, 1196.0, 3957.100000000001, 4371.8, 5327.67, 19.549754124246206, 3138.3130712193783, 10.755478160668902], "isController": false}, {"data": ["7. Logout", 1300, 0, 0.0, 393.7223076923074, 5, 2213, 175.0, 1052.0, 1323.95, 1771.98, 20.26974350978405, 61.32389198565525, 6.809366960318078], "isController": false}, {"data": ["3. Visualizza Catalogo", 1300, 0, 0.0, 6303.743846153845, 38, 19794, 2043.0, 14038.0, 15070.25, 17089.05, 19.64963194727853, 12196.370734234948, 3.3964695846370105], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 1300, 0, 0.0, 1364.303076923077, 105, 6965, 683.0, 3194.5000000000005, 3557.8500000000004, 4606.8, 19.557106751715008, 3136.6824036342373, 3.479437300667951], "isController": false}, {"data": ["1. Home Page", 1300, 0, 0.0, 25327.99923076923, 603, 60243, 20641.0, 58669.8, 60096.45, 60224.97, 20.18194802372155, 58.41727924053777, 2.2862362995622068], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 16900, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
