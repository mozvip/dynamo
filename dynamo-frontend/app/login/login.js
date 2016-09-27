'use strict';

angular.module('dynamo.login', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/login', {
    templateUrl: 'login/login.html',
    controller: 'LoginCtrl'
  });
}])

.controller('LoginCtrl', ['$scope', '$location', function( $scope, $location ) {

    $scope.username = '';
    $scope.password = '';

    $scope.login = function() {
      alert($scope.password);
    }

}]);
