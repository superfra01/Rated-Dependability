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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.07656410256410257, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.11366666666666667, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.04, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.012333333333333333, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.0, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.008666666666666666, 500, 1500, "2. Login"], "isController": false}, {"data": [0.297, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.23766666666666666, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.0, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.0026666666666666666, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.214, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.004666666666666667, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.06433333333333334, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [3.333333333333333E-4, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 19500, 0, 0.0, 23294.615948718012, 3, 135684, 17108.5, 55167.8, 61270.15000000002, 103688.84000000019, 87.35189374426052, 11429.846061535489, 22.856877631755772], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 1500, 0, 0.0, 11609.322000000006, 4, 48913, 8789.0, 28785.600000000002, 29985.4, 32079.91, 13.462453217975067, 38.345352662985434, 2.4584753435169313], "isController": false}, {"data": ["2. Login-0", 1500, 0, 0.0, 7771.62066666666, 13, 46351, 4586.0, 21739.9, 22833.25, 31108.53, 22.65450371533861, 3.3627778952455745, 6.93966739884764], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 1500, 0, 0.0, 35425.02533333343, 116, 93585, 32026.0, 68274.3, 74933.1, 89300.42, 8.995610142250582, 1.2929784015400485, 3.3486158754527793], "isController": false}, {"data": ["5. Ricerca Film", 1500, 0, 0.0, 44698.60533333336, 3607, 71638, 45002.5, 58510.4, 59214.7, 62686.630000000005, 7.957179763300426, 4686.970645507973, 1.49197120561883], "isController": false}, {"data": ["2. Login", 1500, 0, 0.0, 19381.32933333335, 40, 95117, 11429.0, 48248.0, 53004.95000000001, 63042.82, 13.045974012419766, 39.095599062972916, 6.378742359951469], "isController": false}, {"data": ["7. Logout-1", 1500, 0, 0.0, 3374.4733333333356, 3, 43193, 3658.5, 5659.1, 7236.050000000012, 22060.810000000005, 25.23977788995457, 73.05732584553256, 4.165549280666331], "isController": false}, {"data": ["7. Logout-0", 1500, 0, 0.0, 9683.200666666658, 3, 43783, 7134.5, 21736.8, 23487.45, 38141.81, 18.12207026530711, 2.3714427886241722, 3.097033492606195], "isController": false}, {"data": ["4. Dettagli Film", 1500, 0, 0.0, 42040.74533333332, 3832, 84931, 39974.0, 57992.200000000004, 62791.45000000002, 70819.6, 7.841784162732705, 1243.4929017966836, 1.3951636776399368], "isController": false}, {"data": ["6. Aggiungi Recensione", 1500, 0, 0.0, 58133.46466666668, 856, 135684, 54845.0, 107953.3, 115796.55, 127260.5, 8.983649757441457, 1427.5023792634904, 4.942481246631131], "isController": false}, {"data": ["7. Logout", 1500, 0, 0.0, 13058.054666666658, 6, 62106, 11178.0, 27589.500000000004, 36010.650000000016, 49282.08, 18.12141347025068, 54.824354424645115, 6.087662337662337], "isController": false}, {"data": ["3. Visualizza Catalogo", 1500, 0, 0.0, 28422.490000000053, 233, 69414, 29691.0, 51457.8, 58286.6, 62299.18, 9.490607462148294, 5890.744775667744, 1.6404663289064922], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 1500, 0, 0.0, 22708.200666666657, 138, 52624, 25847.0, 42237.3, 44243.9, 48826.92, 11.294585375770854, 1793.0867401802993, 2.009465568456482], "isController": false}, {"data": ["1. Home Page", 1500, 0, 0.0, 6523.47533333333, 1024, 19124, 4868.0, 14255.300000000003, 18545.55, 19082.99, 64.96037417175522, 188.02983305183838, 7.358792386644147], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 19500, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
