var auth = 'Basic WjhlYWYyam1ZOEY1TG90WnJaZHQxbVlKdUVWUStuN1hyckUzL3RTT05tODpaOGVhZjJqbVk4RjVMb3RaclpkdDFtWUp1RVZRK243WHJyRTMvdFNPTm04';

$('body').bind('touchmove', function(e) {
  e.preventDefault()
});

angular.module('hottrade', ['gajus.swing', 'ui.router'])

.config(function($stateProvider, $urlRouterProvider) {

})

.controller('MainCtrl', function($scope, $http) {

  $scope.sectors = ["Consumer, Cyclical", "Financial", "Consumer, Non-cyclical", "Energy", "Technology", "Industrial", "Utilities", "Communications", "Basic Materials", "Diversified"];
  $scope.survey = {
    sector: $scope.sectors[0],
    risk: 'safe'
  };

  $scope.matches = [];

  $scope.matchStocks = function() {
    $http.get('/api/industry/' + $scope.survey.sector).success(function(data) {
      $scope.matches = _.first(data, 20);
      $('#workLink').click();
    });
  };

  $scope.passed = [];
  $scope.picked = [];

  $scope.swipeLeft = function(index) {
    console.log(index);
    $scope.passed.push($scope.matches.splice(index, 1)[0]);
    $scope.$apply();
  };

  $scope.swipeRight = function(index) {
    console.log(index);
    $scope.picked.push($scope.matches.splice(index, 1)[0]);
    $scope.$apply();
  };

  $scope.nextWindow = function() {
    if ($scope.matches.length > 0) {
      return;
    }
    $('#nextLink').click();
  };

});
