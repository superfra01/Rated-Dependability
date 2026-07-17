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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9192307692307692, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.825, 500, 1500, "2. Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [1.0, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.825, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.75, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.975, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.775, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [1.0, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.8, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 260, 0, 0.0, 199.85000000000002, 3, 2514, 48.0, 723.1000000000003, 1046.0, 1871.2699999999932, 0.6977037496209589, 333.00952374004095, 0.1824344359736053], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 20, 0, 0.0, 9.45, 3, 15, 8.0, 15.0, 15.0, 15.0, 0.05424199523755282, 0.15315947756822287, 0.00990552061467029], "isController": false}, {"data": ["2. Login-0", 20, 0, 0.0, 370.4, 5, 1047, 16.0, 1046.9, 1047.0, 1047.0, 0.05408855378024902, 0.008028769701755714, 0.016501234571240034], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 20, 0, 0.0, 161.25000000000003, 5, 464, 129.5, 399.0000000000001, 460.99999999999994, 464.0, 0.05444458358060249, 0.007826408889711606, 0.02026784693449772], "isController": false}, {"data": ["5. Ricerca Film", 20, 0, 0.0, 61.949999999999996, 23, 91, 62.0, 74.9, 90.19999999999999, 91.0, 0.054446213946397704, 73.57900166911675, 0.01020866511494957], "isController": false}, {"data": ["2. Login", 20, 0, 0.0, 381.25, 9, 1064, 25.0, 1064.0, 1064.0, 1064.0, 0.054088114948061886, 0.16075368069622223, 0.026378520122130964], "isController": false}, {"data": ["7. Logout-1", 20, 0, 0.0, 6.500000000000001, 3, 12, 6.5, 10.0, 11.899999999999999, 12.0, 0.0544684559554448, 0.15638404346582785, 0.008989422906709153], "isController": false}, {"data": ["7. Logout-0", 20, 0, 0.0, 6.999999999999999, 3, 20, 6.0, 11.900000000000002, 19.599999999999994, 20.0, 0.05446712092245516, 0.007127533401961906, 0.009308345860771144], "isController": false}, {"data": ["4. Dettagli Film", 20, 0, 0.0, 362.44999999999993, 42, 688, 368.0, 684.9, 687.85, 688.0, 0.054355949938170105, 20.339279860101374, 0.009671537186264251], "isController": false}, {"data": ["6. Aggiungi Recensione", 20, 0, 0.0, 216.9, 59, 501, 195.0, 428.6, 497.49999999999994, 501.0, 0.0544373222281196, 20.46721841869786, 0.02995115951496346], "isController": false}, {"data": ["7. Logout", 20, 0, 0.0, 14.250000000000004, 7, 31, 13.5, 23.900000000000002, 30.649999999999995, 31.0, 0.054466527595466205, 0.16350596272310852, 0.01829734911410193], "isController": false}, {"data": ["3. Visualizza Catalogo", 20, 0, 0.0, 520.2500000000001, 136, 984, 270.5, 981.5, 983.9, 984.0, 0.05422420080306041, 201.30453366001154, 0.009372737834122747], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 20, 0, 0.0, 54.85000000000001, 26, 93, 52.5, 79.00000000000003, 92.35, 93.0, 0.054459556971504036, 20.467749624739614, 0.00968997195332816], "isController": false}, {"data": ["1. Home Page", 20, 0, 0.0, 431.55, 6, 2514, 16.0, 2122.700000000001, 2496.8999999999996, 2514.0, 0.05372228265979021, 0.15424170998025705, 0.00608572733255436], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 260, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
