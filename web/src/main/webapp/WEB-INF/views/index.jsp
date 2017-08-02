<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Streaming Charts</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="/js/jquery-3.2.1.min.js"></script>
<script src="/js/anychart.dev.min.js"></script>
<script src="/js/anystock.dev.min.js"></script>
<link rel="stylesheet" href="https://cdn.anychart.com/css/7.12.0/anychart-ui.min.css">
<script type="text/javascript">
var dataTable;
var chart;

function updateChartData() {
    var security = $('#security').val();
    var url = '/securities/' + security;

    $.ajax({
        url : url,
        type: 'GET',
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        
        success: function(data) {
            dataTable.addData(data);
        },
        error: function(msg) {
            alert('Error ' + msg);
        },
        complete: poll()
    });
};

function poll() {
    setTimeout(updateChartData, 30000);
}
function setChartName(security) {
	chart.title(security + ' Stock Prices');
} 
anychart.onDocumentReady(function() {
	dataTable = anychart.data.table('date');
	updateChartData();
	chart = anychart.stock();
    var security = $('#security').val();
    setChartName(security);
    
	var plot1 = chart.plot(0);
    var ohlcMapping = dataTable.mapAs({'open': "open", 'high': "high", 'low': "low", 'close': "close"});
    
    plot1.ohlc(ohlcMapping);
	
	var plot2 = chart.plot(1);
	var columnMapping = dataTable.mapAs({'individualIndex': 4});
	var lineMapping = dataTable.mapAs({'marketIndex': 3});
    
    plot2.column(columnMapping);
    plot2.line(lineMapping);

	chart.container('container');

	chart.draw();
});
$(document).on('change', '#security', function() {
	dataTable.remove();
    updateChartData();
    setChartName($(this).val());
});
</script>
<style type="text/css">
    body {
    	font: 12px verdana, sans-serif;
    	margin: 0px;
    }
    
    .header {
    	padding: 10px 0;
    	background-color: #cdecf5;
    }
    
    .header h1 {
    	font-size: 18px;
    	margin: 10px;
    }
    
    .content {
    	width: 100%;
    }
    
    .settings {
    	padding: 10px;
    }
    
    div#container {
        padding: 10px;
    }
    
    .footer {
        position: fixed;
        width: 100%;
    	left: 0px;
    	bottom: 0px;
    	height: 30px;
    	background-color: #cdecf5;
    	padding: 10px 0;
    }
    
    .footer p {
    	text-align: center;
    	margin: 5px;
    }
</style>
</head>
<body>
    <div class="content">
        <div class="header">
            <h1>Streaming Charts</h1>
        </div>
        <div class="settings">
            <select id="security">
                <c:forEach var="security" items="${securities}">
                    <option value="${security}">${security}</option>
                </c:forEach>
            </select>
        </div>
        <div id="container"></div>
        <div class="footer">
            <p>&copy; GigaSpaces Technologies</p>
        </div>
    </div>
</body>
</html>
