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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.7144230769230769, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "2. Login-1"], "isController": false}, {"data": [1.0, 500, 1500, "2. Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.175, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.99, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.5825, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.58, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.98, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.1, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.6425, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.2375, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 2600, 0, 0.0, 918.5650000000003, 4, 10612, 253.5, 2099.8, 3001.499999999998, 10155.679999999993, 143.18757572419872, 18583.867830725438, 37.46621453009693], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 200, 0, 0.0, 112.91500000000003, 18, 427, 106.0, 185.9, 225.64999999999992, 379.30000000000064, 13.67147446852143, 38.94013763329688, 2.4966462164194407], "isController": false}, {"data": ["2. Login-0", 200, 0, 0.0, 120.57999999999997, 16, 358, 108.0, 180.0, 245.89999999999975, 313.9200000000001, 13.7807482946324, 2.0455798249844968, 4.220825186901399], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 200, 0, 0.0, 115.41999999999996, 11, 424, 101.5, 193.0, 230.95, 367.39000000000055, 13.749484394335212, 1.9762869732228792, 5.1182589930221365], "isController": false}, {"data": ["5. Ricerca Film", 200, 0, 0.0, 1737.8950000000004, 484, 3028, 1730.5, 2543.9000000000005, 2766.75, 2932.79, 12.142553579017667, 7152.256178947089, 2.2767287960658127], "isController": false}, {"data": ["2. Login", 200, 0, 0.0, 233.96500000000003, 61, 701, 223.5, 350.8, 427.24999999999983, 623.3900000000006, 13.610997686130393, 40.78826551228392, 6.654435377535048], "isController": false}, {"data": ["7. Logout-1", 200, 0, 0.0, 111.53000000000003, 5, 427, 94.0, 205.1000000000001, 289.79999999999995, 358.93000000000006, 14.398848092152628, 41.67791576673866, 2.376372390208783], "isController": false}, {"data": ["7. Logout-0", 200, 0, 0.0, 105.31999999999995, 4, 424, 94.5, 158.9, 217.89999999999998, 361.5800000000004, 14.180374361883155, 1.8556349262620535, 2.423403821610891], "isController": false}, {"data": ["4. Dettagli Film", 200, 0, 0.0, 737.38, 279, 1794, 631.5, 1186.5, 1424.5, 1764.5700000000004, 12.109469605231292, 1866.0170559041235, 2.1544567578408818], "isController": false}, {"data": ["6. Aggiungi Recensione", 200, 0, 0.0, 783.3700000000001, 192, 2650, 701.0, 1297.2000000000003, 1728.8999999999965, 2452.980000000002, 13.612850530901172, 2099.6382225445823, 7.489327737884563], "isController": false}, {"data": ["7. Logout", 200, 0, 0.0, 217.27500000000006, 9, 851, 193.0, 358.30000000000007, 487.5999999999999, 638.6800000000003, 14.1753490679708, 42.88596817634134, 4.7620313275214405], "isController": false}, {"data": ["3. Visualizza Catalogo", 200, 0, 0.0, 1909.900000000001, 626, 3072, 1838.5, 2654.9, 2838.25, 3060.4100000000008, 12.254901960784313, 7606.519930970435, 2.1182789522058822], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 200, 0, 0.0, 667.4949999999998, 146, 2441, 589.5, 1095.1000000000001, 1565.5499999999972, 2307.940000000002, 13.742870885728028, 2117.7171507807666, 2.4450634104651963], "isController": false}, {"data": ["1. Home Page", 200, 0, 0.0, 5088.3, 12, 10612, 5679.5, 10334.9, 10478.9, 10603.98, 13.740038472107722, 39.77097073371806, 1.556488733168453], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 2600, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
