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

    var data = {"OkPercent": 99.26054974835462, "KoPercent": 0.7394502516453736};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.4475996902826171, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.6885, 500, 1500, "2. Login-1"], "isController": false}, {"data": [0.49775, 500, 1500, "2. Login-0"], "isController": false}, {"data": [0.6240208877284595, 500, 1500, "6. Aggiungi Recensione-0"], "isController": false}, {"data": [0.12175, 500, 1500, "5. Ricerca Film"], "isController": false}, {"data": [0.44575, 500, 1500, "2. Login"], "isController": false}, {"data": [0.827, 500, 1500, "7. Logout-1"], "isController": false}, {"data": [0.72775, 500, 1500, "7. Logout-0"], "isController": false}, {"data": [0.27925, 500, 1500, "4. Dettagli Film"], "isController": false}, {"data": [0.29925, 500, 1500, "6. Aggiungi Recensione"], "isController": false}, {"data": [0.66725, 500, 1500, "7. Logout"], "isController": false}, {"data": [0.10025, 500, 1500, "3. Visualizza Catalogo"], "isController": false}, {"data": [0.477023498694517, 500, 1500, "6. Aggiungi Recensione-1"], "isController": false}, {"data": [0.072, 500, 1500, "1. Home Page"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 25830, 191, 0.7394502516453736, 4342.008865660143, 1, 44298, 654.0, 12842.200000000012, 21589.100000000013, 34635.0, 100.21377386527203, 13113.745212760281, 25.95910592766606], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["2. Login-1", 2000, 0, 0.0, 1112.3730000000016, 4, 9725, 347.0, 1608.9, 5491.199999999975, 9621.99, 8.021143734885158, 22.846849294740938, 1.464798709397973], "isController": false}, {"data": ["2. Login-0", 2000, 0, 0.0, 1342.3309999999974, 7, 4559, 1077.5, 2972.5000000000005, 3536.8999999999996, 4063.0, 7.973368947714634, 1.183546953176391, 2.4425294516315503], "isController": false}, {"data": ["6. Aggiungi Recensione-0", 1915, 0, 0.0, 2859.5493472584817, 7, 19541, 351.0, 11518.800000000001, 14121.199999999986, 17983.479999999996, 8.145539307012395, 1.1707966597971908, 3.032179665501195], "isController": false}, {"data": ["5. Ricerca Film", 2000, 0, 0.0, 9176.222500000016, 229, 26174, 4680.5, 21604.2, 22541.8, 24078.89, 8.243751236562685, 4855.768230025391, 1.5457033568555034], "isController": false}, {"data": ["2. Login", 2000, 0, 0.0, 2455.001999999998, 46, 14047, 1662.5, 4005.4000000000005, 8884.04999999997, 13531.9, 7.971843448938349, 23.88974641565989, 3.897857766369184], "isController": false}, {"data": ["7. Logout-1", 2000, 0, 0.0, 432.0640000000005, 2, 9354, 116.0, 1060.9, 1375.4999999999982, 6559.780000000001, 8.832907881603703, 25.56712789167322, 1.4577748359287361], "isController": false}, {"data": ["7. Logout-0", 2000, 0, 0.0, 1387.1360000000038, 2, 15559, 124.0, 6594.400000000001, 8080.0, 11738.73, 8.795654946456452, 1.1509939090089496, 1.5031636871385536], "isController": false}, {"data": ["4. Dettagli Film", 2000, 106, 5.3, 5066.747999999992, 2, 23875, 2593.0, 11736.7, 14906.599999999995, 19851.0, 8.105205568276226, 1234.6109849597779, 1.365624240136978], "isController": false}, {"data": ["6. Aggiungi Recensione", 2000, 85, 4.25, 4315.389499999996, 1, 27187, 1564.5, 11874.100000000006, 14386.649999999983, 22428.440000000002, 8.50296753566995, 1331.292347103358, 4.479216322668487], "isController": false}, {"data": ["7. Logout", 2000, 0, 0.0, 1819.4294999999968, 5, 16325, 245.0, 7670.6, 9029.199999999997, 12479.340000000002, 8.795538902668566, 26.609940937956267, 2.9547513501152216], "isController": false}, {"data": ["3. Visualizza Catalogo", 2000, 0, 0.0, 11718.16750000002, 540, 37555, 4872.5, 30040.6, 31747.09999999999, 34239.8, 7.998656225754074, 4964.702554970765, 1.3825802265219442], "isController": false}, {"data": ["6. Aggiungi Recensione-1", 1915, 0, 0.0, 1610.9671018276708, 99, 11320, 957.0, 3798.0000000000005, 4729.5999999999985, 9640.159999999998, 8.315674286111303, 1357.808618990134, 1.4794781086617625], "isController": false}, {"data": ["1. Home Page", 2000, 0, 0.0, 12971.662000000006, 200, 44298, 5234.0, 34592.4, 37341.44999999998, 43629.75, 7.804634391901912, 22.590758142184832, 0.8841187397076384], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 191, 100.0, 0.7394502516453736], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 25830, 191, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 191, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["4. Dettagli Film", 2000, 106, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 106, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["6. Aggiungi Recensione", 2000, 85, "Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: localhost:8080 failed to respond", 85, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
