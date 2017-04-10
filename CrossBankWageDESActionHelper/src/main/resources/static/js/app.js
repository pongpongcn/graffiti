// Define the `simpleApp` module
var simpleApp = angular.module('simpleApp', []);

// Define the `HelperController` controller on the `simpleApp` module
simpleApp.controller('EncryptHelperController', function EncryptHelperController($scope, $http) {
	$scope.key = "shanghai";
	$scope.padding = " ";

	$scope.update = function() {
		var request = {
			"data" : $scope.data,
			"key" : $scope.key,
			"padding" : $scope.padding,
			"paddingLeft" : $scope.paddingLeft
		};
		
		$scope.encryptedData = null;
		$scope.adjustedData = null;
		
		$http.post('/api/encrypt', request).then(function(response) {
			$scope.encryptedData = response.data.encryptedData;
			$scope.adjustedData = response.data.adjustedData;
		});
	};
});

simpleApp.controller('DecryptHelperController', function DecryptHelperController($scope, $http) {
	$scope.key = "shanghai";

	$scope.update = function() {
		var request = {
			"encryptedData" : $scope.encryptedData,
			"key" : $scope.key
		};
		
		$scope.data = null;
		
		$http.post('/api/decrypt', request).then(function(response) {
			$scope.data = response.data.data;
		});
	};
});