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

    var data = {"OkPercent": 99.8021978021978, "KoPercent": 0.1978021978021978};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.4147252747252747, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.6142857142857143, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.43214285714285716, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.4585714285714286, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.2542857142857143, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.34, 500, 1500, "2. Login"], "isController": false}, {"data": [0.8942857142857142, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.8642857142857143, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.12428571428571429, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.06785714285714285, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.7064285714285714, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.24285714285714285, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.2914285714285714, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.10071428571428571, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 9100, 18, 0.1978021978021978, 3513.9821978022037, 2, 30141, 1421.0, 11348.0, 14205.54999999999, 19061.97, 165.11231266102985, 21623.652438014844, 43.13319962373444], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 700, 0, 0.0, 828.5099999999998, 2, 3373, 1020.5, 1870.0, 2021.9499999999998, 3302.86, 16.792208415295306, 47.82954146775896, 3.0665458727150603], "isController": false}, {"data": ["2. Login-0", 700, 0, 0.0, 1417.7871428571425, 7, 8750, 1463.0, 2487.7, 2978.7499999999995, 8689.69, 16.969285593076535, 2.550837782720419, 5.175537411214274], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 700, 0, 0.0, 3623.571428571428, 8, 18492, 857.5, 11757.3, 13728.8, 17987.77, 13.39687278712369, 1.9255948749784695, 4.986989632973532], "isController": false}, {"data": ["5. Ricerca Film", 700, 0, 0.0, 5809.949999999999, 24, 19604, 7118.0, 11577.7, 14492.05, 18175.98, 13.353172332226928, 7865.340298741464, 2.503719812292549], "isController": false}, {"data": ["2. Login", 700, 0, 0.0, 2247.095714285714, 10, 9287, 2905.0, 4025.0, 4880.7, 9226.69, 16.7500179464478, 50.227246895264535, 8.167503170539112], "isController": false}, {"data": ["7. Logout-1", 700, 0, 0.0, 324.1757142857148, 2, 2350, 210.0, 1010.6999999999999, 1272.7999999999997, 1690.8600000000001, 13.568521031207599, 39.27450814111262, 2.239335990502035], "isController": false}, {"data": ["7. Logout-0", 700, 0, 0.0, 367.2885714285713, 2, 2427, 225.5, 1063.7999999999997, 1276.7999999999997, 2306.92, 13.557746315198234, 1.7741582092153938, 2.316997661288761], "isController": false}, {"data": ["4. Dettagli Film", 700, 0, 0.0, 3834.940000000006, 167, 14737, 3905.0, 7029.9, 8222.8, 10527.41, 13.499961428681633, 2149.126095967851, 2.401836748100363], "isController": false}, {"data": ["6. Aggiungi Recensione", 700, 0, 0.0, 5833.275714285719, 229, 24432, 3247.5, 14327.299999999994, 16286.799999999997, 18826.31, 13.336127569586008, 2124.9614316511556, 7.3370655637371645], "isController": false}, {"data": ["7. Logout", 700, 0, 0.0, 691.8671428571431, 5, 3897, 408.5, 1966.9999999999995, 2602.549999999995, 3587.8100000000004, 13.556433496010536, 41.013506807266246, 4.554114377566039], "isController": false}, {"data": ["3. Visualizza Catalogo", 700, 0, 0.0, 9641.162857142857, 21, 21261, 10056.0, 19037.0, 19659.6, 20129.38, 13.85343070316056, 8598.714545421935, 2.3945871430267767], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 700, 0, 0.0, 2209.455714285713, 99, 14206, 1786.5, 4468.0999999999985, 5933.949999999993, 10874.250000000002, 13.38432122370937, 2130.716755795889, 2.3812626971797326], "isController": false}, {"data": ["1. Home Page", 700, 18, 2.5714285714285716, 8852.688571428578, 342, 30141, 10378.5, 13461.9, 13983.4, 30030.99, 21.107224701483535, 60.91954943613557, 2.3295685834036908], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 18, 100.0, 0.1978021978021978], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 9100, 18, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 18, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["1. Home Page", 700, 18, "Non HTTP response code: java.net.SocketException/Non HTTP response message: Connection reset", 18, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
