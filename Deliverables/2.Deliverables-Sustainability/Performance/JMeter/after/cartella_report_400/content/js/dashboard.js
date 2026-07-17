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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.6928846153846154, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.99625, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.9925, 500, 1500, "2. Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.14, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.93, 500, 1500, "2. Login"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [1.0, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.52125, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.515, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.98375, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.095, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.5925, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.24125, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 5200, 0, 0.0, 1629.937115384618, 3, 27184, 315.0, 3188.800000000001, 5223.649999999962, 24515.45999999999, 154.51357936649433, 20056.274653458724, 40.42964107164081], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 400, 0, 0.0, 159.73249999999993, 54, 746, 126.0, 274.0, 451.0, 498.95000000000005, 12.809837955549863, 36.48636527853392, 2.339296579773266], "isController": false}, {"data": ["2. Login-0", 400, 0, 0.0, 155.9825000000001, 23, 764, 130.0, 253.90000000000003, 282.69999999999993, 566.8300000000002, 12.807787134577824, 1.9011559027888956, 3.923229073276552], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 400, 0, 0.0, 148.92749999999992, 17, 468, 131.0, 259.0, 291.79999999999995, 425.8600000000001, 14.992503748125937, 2.1547331802848575, 5.580754544602699], "isController": false}, {"data": ["5. Ricerca Film", 400, 0, 0.0, 2427.494999999997, 239, 4722, 2165.5, 4198.8, 4344.9, 4508.0, 12.929083974400413, 7615.541914221589, 2.4242032452000775], "isController": false}, {"data": ["2. Login", 400, 0, 0.0, 317.0399999999999, 108, 973, 267.0, 582.6000000000012, 686.95, 823.9200000000001, 12.773839177364756, 38.27994641973239, 6.245552857747334], "isController": false}, {"data": ["7. Logout-1", 400, 0, 0.0, 127.665, 3, 480, 110.0, 218.90000000000003, 254.95, 428.1200000000008, 15.554518587649712, 45.023040130657954, 2.5671031653445326], "isController": false}, {"data": ["7. Logout-0", 400, 0, 0.0, 127.47500000000004, 3, 422, 116.5, 226.7000000000001, 256.0, 328.95000000000005, 15.443419173004903, 2.0209161808424385, 2.63925620632408], "isController": false}, {"data": ["4. Dettagli Film", 400, 0, 0.0, 986.9975000000007, 201, 3630, 883.0, 1562.4, 1788.9, 3365.55, 12.68713524486171, 1955.900682528229, 2.2570463159731036], "isController": false}, {"data": ["6. Aggiungi Recensione", 400, 0, 0.0, 941.5249999999999, 200, 2991, 857.5, 1575.0, 1755.95, 2467.7400000000002, 14.889261120416899, 2297.529313232831, 8.191129257398103], "isController": false}, {"data": ["7. Logout", 400, 0, 0.0, 255.4724999999998, 7, 659, 240.5, 434.90000000000003, 476.84999999999997, 618.8900000000001, 15.441034549314805, 46.71516116579811, 5.187222543910441], "isController": false}, {"data": ["3. Visualizza Catalogo", 400, 0, 0.0, 2681.0999999999985, 863, 4669, 2576.0, 4263.7, 4438.75, 4556.97, 12.4000248000496, 7696.596913701639, 2.143363661727323], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 400, 0, 0.0, 792.2700000000002, 155, 2719, 715.5, 1340.0, 1613.3999999999999, 2368.3000000000015, 14.97959030820507, 2309.3149353537056, 2.664874967232146], "isController": false}, {"data": ["1. Home Page", 400, 0, 0.0, 12067.499999999995, 73, 27184, 13027.0, 25202.100000000002, 26220.45, 27013.68, 12.7420998980632, 36.88240634556575, 1.443441004077472], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 5200, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
