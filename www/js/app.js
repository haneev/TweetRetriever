var app = angular.module("app", ['ngMaterial','ngMessages']);

function cloneObject(oldObject) {
	return JSON.parse(JSON.stringify(oldObject));
}

app.config(function($mdThemingProvider, $mdIconProvider){
	
    $mdIconProvider.icon("menu", "./images/menu.svg", 24)
    .icon("settings", "./images/ic_settings_black_24px.svg", 24)
    .icon("stop", "./images/ic_stop_black_24px.svg", 24)
    .icon("pause", "./images/ic_pause_black_24px.svg", 24)
    .icon("reset", "./images/ic_cancel_black_24px.svg", 24)
    .icon("close", "./images/ic_clear_white_24px.svg", 24)
    .icon("start", "./images/ic_play_arrow_black_24px.svg", 24);
    
    $mdThemingProvider.theme('default')
        .primaryPalette('blue')
        .accentPalette('blue-grey');
    
    $mdThemingProvider.theme('docs-dark', 'default')
	    .primaryPalette('blue-grey')
	    .accentPalette('blue')
	    .dark();
    
});

app.run(function ($rootScope) {
	$rootScope.running = false;
	$rootScope.status = 'stopped';
	$rootScope.settings = {};
	$rootScope.threshold = 0.8; // threshold for declining a keyword
	
	// Do action on receiving a status
	$rootScope.$on("status", function (event, status) {
		$rootScope.status = status;
		
		if(status == 'stopped') {
			$rootScope.$broadcast('action', 'stop');
		} else if(status == 'running') {
			$rootScope.running = true;
			$rootScope.$broadcast('action', 'startOverview');
		}	
		
	});
});

app.controller("BaseController",  function ($scope, $mdUtil, $mdSidenav, $rootScope, ServerCommand, $mdDialog) {

	var logInterval = 2000;
	
	$scope.toggleLeft = $mdUtil.debounce(function() {
		$mdSidenav('left').toggle()
	}, 300);	
	
	$scope.reset = function () {
		$scope.$emit("status", "stopped");
		$rootScope.running = false;
		$rootScope.settings = {};
	};
	
	$scope.stop = function () {
		ServerCommand.stop().then(function () {
			$scope.$emit("status", "stopped");
		});
	};
	
	$scope.resume = function () {
		$rootScope.status = "running";
		$rootScope.running = true;
		$rootScope.$broadcast('action', 'startOverview');
	}

	$scope.logs = function($event) {
		$mdDialog.show({
			scope : $scope,
			controller : function($scope, $mdDialog, $interval, $http, $timeout) {

				function scrollBottom() {
					var element = document.querySelector(".logScroll");
					element.scrollTop = element.scrollHeight;
				}
				
				// load logs every 2 second
				$scope.data = {};
				var int = $interval(function() {
					$http.get('/api/log').success(function(data) {
						$scope.data = data;
						
						$timeout(function () {
							scrollBottom();
						}, 50);
					});
				}, logInterval);

				$scope.close = function() {
					$interval.cancel(int);
					$mdDialog.cancel();
				}
			},
			templateUrl : 'logs.html',
			parent : angular.element(document.body),
			targetEvent : event,
			clickOutsideToClose : true
		});
	};
	
});

app.controller("FormController", function ($scope, $http, $rootScope, ServerCommand, $interval) {
	
	var waitInterval = 1500;
	
	// wait for end initialization phase
	function statusPolling() {
		
		var int = $interval(function () {			
			ServerCommand.status().success(function (data) {
				if(data.status == 'running') {
					$scope.$emit("status", 'running');
					$interval.cancel(int);
				}
			});
			
		}, waitInterval);
		
	}
	
	$scope.startApp = function () {
		
		if(!$scope.start.$invalid) {
			var data = angular.extend({top:10,training:2500}, $scope.data);
			ServerCommand.start(data).success(function () {
				$rootScope.settings = data;
				$scope.$emit("status", 'starting');				
				statusPolling();
			});
		}
		
	};
	
	// Get trending topics
	$scope.trending = {};
	$scope.getTrending = function () {
		$http.get('/api/twitter').success(function (trends) {
			$scope.trending = trends;
		});
	};
	
});

app.controller("RunningController", function ($scope, $interval, ServerCommand, Tweet) {
	
	$scope.current = {};
	
	var dashboardInterval = 1000;
	var poller;
	function startPolling() {
		poller = $interval(function (){
			ServerCommand.status().success(function (data) {
				$scope.current = data;
			});
		}, dashboardInterval);
	}
	
	function stopPolling() {
		if(poller)
			$interval.cancel(poller);
	}
	
	// on receive action, do something
	$scope.$on('action', function (event, action) {
		if(action == 'startOverview') {
			startPolling();
		} else if(action == 'stop') {
			stopPolling();
		}
	});
	
	$scope.openTweet = function ($event, tweet) {
		Tweet.open($event, tweet);
	};
	
});

app.filter('unsafe', function($sce) { 
	return $sce.trustAsHtml; 
});

/*
 * Open a Tweet and show embed
 */
app.factory('Tweet', function ($http, $mdDialog, $q) {
	
	return {	
		open : function (event, original_tweet) {
			var tweet = cloneObject(original_tweet);
			var url = "https://api.twitter.com/1/statuses/oembed.json?id="+tweet.id_str+"&callback=JSON_CALLBACK";
			$http.jsonp(url).success(function (data) {
				$mdDialog.show({
				      controller: function ($scope, $mdDialog, tweet) {
				    	  
				    	  if(twttr)
				    		  twttr.widgets.load();
				    	  
				    	  $scope.tweet = tweet;
				    	  $scope.close = function () {
				    		  $mdDialog.cancel();
				    	  }
				      },
				      locals : {
				    	  tweet : data
				      },
				      templateUrl: 'tweetdialog.html',
				      parent: angular.element(document.body),
				      targetEvent: event,
				      clickOutsideToClose:true
			    })
			});
		}		
	};
	
});

/*
 * Command a server
 * Start/stop/status
 */
app.factory('ServerCommand', function ($http) {
	
	var command = function (cmd) {
		return $http.post('/api/status', {command:cmd});
	}
	
	return {
		start : function (data) {
			return $http.post('/api/status', angular.extend({command:'start'}, data));
		},
		stop : function () {
			return command('stop');
		},
		status : function () {
			return $http.get('/api/status');
		}
	};
	
});