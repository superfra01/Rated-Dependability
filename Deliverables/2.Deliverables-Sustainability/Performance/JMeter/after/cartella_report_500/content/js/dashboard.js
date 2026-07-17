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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.6836153846153846, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.999, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.998, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.998, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.097, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.984, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.509, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.516, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.99, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.049, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.588, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.159, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 6500, 0, 0.0, 1733.274307692309, 2, 29697, 287.0, 2560.5000000000027, 9573.299999999994, 26501.479999999967, 171.53563982793656, 22700.359529010966, 44.88373306085557], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 500, 0, 0.0, 128.15799999999996, 10, 515, 116.5, 197.90000000000003, 214.0, 342.93000000000006, 14.832833961256638, 42.248488441514134, 2.7087304206591707], "isController": false}, {"data": ["2. Login-0", 500, 0, 0.0, 131.34799999999996, 12, 509, 120.0, 183.0, 235.64999999999992, 393.94000000000005, 14.929384013615598, 2.2160804395210656, 4.573115258576931], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 500, 0, 0.0, 137.15199999999993, 13, 576, 122.0, 211.0, 279.69999999999993, 489.98, 15.176809834572774, 2.181251422825922, 5.649389607679466], "isController": false}, {"data": ["5. Ricerca Film", 500, 0, 0.0, 1964.2499999999995, 813, 3206, 1926.5, 2752.8, 2870.5499999999997, 3091.86, 14.130281192595733, 8323.07601870496, 2.6494277236117], "isController": false}, {"data": ["2. Login", 500, 0, 0.0, 259.8980000000001, 80, 761, 242.0, 360.0, 439.0, 692.9200000000001, 14.812620352540366, 44.38966217967708, 7.242387701821952], "isController": false}, {"data": ["7. Logout-1", 500, 0, 0.0, 119.45199999999993, 2, 496, 114.0, 185.90000000000003, 215.89999999999998, 358.9200000000001, 15.493786991416442, 44.84725062749837, 2.557080079638065], "isController": false}, {"data": ["7. Logout-0", 500, 0, 0.0, 118.57599999999991, 3, 474, 111.5, 188.90000000000003, 217.95, 326.83000000000015, 15.416872224962999, 2.017442263813517, 2.634719374383325], "isController": false}, {"data": ["4. Dettagli Film", 500, 0, 0.0, 906.0199999999998, 230, 2594, 794.0, 1517.8000000000004, 1867.85, 2327.3000000000006, 14.185201997276442, 2342.5775875138306, 2.52358068188266], "isController": false}, {"data": ["6. Aggiungi Recensione", 500, 0, 0.0, 904.3500000000005, 161, 2605, 838.5, 1462.6000000000004, 1723.3499999999997, 2178.91, 15.116700931188777, 2498.580122530309, 8.316311298222276], "isController": false}, {"data": ["7. Logout", 500, 0, 0.0, 238.34800000000013, 6, 703, 230.0, 354.90000000000003, 405.9, 598.8600000000001, 15.415446277169725, 46.63774664714043, 5.1786264837367035], "isController": false}, {"data": ["3. Visualizza Catalogo", 500, 0, 0.0, 2236.2639999999965, 862, 3527, 2240.5, 3063.2000000000003, 3167.0, 3376.7200000000003, 14.056789429294348, 8724.937568087575, 2.4297380165870117], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 500, 0, 0.0, 766.8960000000001, 126, 2292, 694.0, 1306.2000000000007, 1505.1499999999999, 2048.090000000001, 15.16024377671993, 2503.598277938434, 2.6970428996998272], "isController": false}, {"data": ["1. Home Page", 500, 0, 0.0, 14621.85399999999, 111, 29697, 15848.0, 28181.000000000007, 29495.5, 29662.97, 14.876967478949092, 43.06184727305186, 1.6852814722247016], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 6500, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
