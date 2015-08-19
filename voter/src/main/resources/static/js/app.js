angular.module('app', []).controller('home', function($scope, $http, $timeout) {

	$scope.elections = [ {
		id : 0,
		name : "US Democratic Primary"
	}, {
		id : 1,
		name : "UK EU Membership"
	} ];
	var candidates = {
		0 : [ {
			id : 0,
			name : "Biden"
		}, {
			id : 1,
			name : "Clinton"
		} ],
		1 : [ {
			id : 0,
			name : "In"
		}, {
			id : 1,
			name : "Out"
		} ]
	};
	$scope.score = 1;
	$scope.message = "";
	$scope.election = $scope.elections[0];

	$scope.update = function() {
		$scope.candidates = candidates[$scope.election.id];
		$scope.candidate = $scope.candidates[0];
	}
	$scope.update();

	var message = function(value) {
		$scope.message = "Success!";
		$scope.showMessage = true;
		$timeout(function() {
			$scope.showMessage = false;
		}, 2000);
	}

	$scope.send = function() {

		$http.post("votes", {
			election : $scope.election.id,
			candidate : $scope.candidate.id,
			score : $scope.score
		}).success(function(metrics) {
			message("Success!");
		}).error(function(metrics) {
			message("Oops!");
		});

	};
});
