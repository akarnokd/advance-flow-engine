var r = 320;

var svg = d3.select("#chart").append("svg")
    .attr("width", r*4)
    .attr("height", r*3.2)
    .style("background","#fff")
    .append("g")
    .attr("transform", "translate(" + r*2 + "," + (r*1.6) + ")")

svg.append("text").attr("class","region")
    .text("MIDWEST")
    .attr("x", r*1.5)
    .attr("y", r*1) 
    .attr("text-anchor","middle");

svg.append("text").attr("class","region")
    .text("SOUTH")
    .attr("x", -r*1.5)
    .attr("y", -r*1) 
    .attr("text-anchor","middle");

svg.append("text").attr("class","region")
    .text("EAST")
    .attr("x", r*1.5)
    .attr("y", -r*1) 
    .attr("text-anchor","middle");

svg.append("text")
    .text("WEST").attr("class","region")
    .attr("x", -r*1.5)
    .attr("y", r*1) 
    .attr("text-anchor","middle");

function Team(name) {
    this.name = name;
    this.seed = 0;
    this.region = 0; 
    this.probs = []; // prob of making it to each game
    this.stillin = 1; // is team still in tournament?
}

function Game(num, flag) {
    this.name = num;
    this.region = 0;
    this.teams = [];     // possible teams playing in this game
    this.prevGames = []; // parent games
    this.played = flag;  // has the game been played?
}

function isDefined(element, index, array) {
    return (element != undefined);        
}

function hasValue(element, index, array) {
    if (element > 0) {
        return 1;
    } else {
        return 0;
    }
}

// recursively create the tree structure so D3 can display it correctly
function travelTree(onegame) {
    //document.write(onegame.name);
    var returnArray = [];
    var h;
    // figure out all the child games first
    if (onegame.prevGames.length > 0) {
            onegame.prevGames.forEach(function (g) {
            //document.write("-->");
            h = new Object();
            h.name = g.name;
            h.children = travelTree(g);
            h.teams = g.teams;
            h.region = g.region;
            h.played = g.played;
            returnArray.push(h);
        });
    } 
    // in the first round, some games are not symmetric, so need to allow
    // for specifying a team as a child
    if (onegame.prevGames.length < 2) {
        onegame.teams.forEach(function (t){
            if (t.probs[onegame.name] >0) {
                var skipThis = false;
                if (onegame.prevGames.length == 1) {
                    onegame.prevGames[0].teams.forEach(function (t2) {
                        if (t2.name == t.name) {
                            skipThis = true;
                        }
                    });
                }
                if (!skipThis) {
                    h = new Object();
                    h.name = t.name;
                    h.children = [];
                    h.stillin = t.stillin;
                    h.probs = t.probs;
                    h.seed = t.seed;
                    h.region = t.region;
                    returnArray.push(h);
                }
            } 
            })
    } 
    return returnArray;

}


function parseCSV(onerow, rowindex) {            
    if (rowindex == 0){
        // first row has all the teams
        onerow.forEach(function(oneteam, teamindex){
            if (teamindex > 0) {  // ignore entry one
                allTeams.push(new Team(oneteam)); 
            }
        });
    } else if (rowindex == 1) {
        // second row is each team's seed
        onerow.forEach(function (oneseed, teamindex) {
            if (teamindex > 0) {  // ignore entry one
                allTeams[teamindex-1].seed = oneseed;
            }
        });
    } else if (rowindex == 2) {
        // second row is each team's regions
        onerow.forEach(function (oneregion, teamindex) {
            if (teamindex > 0) {  // ignore entry one
                allTeams[teamindex-1].region = oneregion;
            }
        });
    } else if (rowindex == 3) {
        // third row indicates if a team is still in the tournament
        onerow.forEach(function (oneflag, teamindex) {
            if (teamindex > 0) {  // ignore entry one
                allTeams[teamindex-1].stillin = oneflag;
            }
        });                
    } else {
        realrowindex = rowindex-4;
        // all other rows are games
        onerow.forEach(function(oneprob, teamindex) {

            if (teamindex == 0) { // first entry is the played flag
                allGames.push(new Game(realrowindex, oneprob));
            } else {
                var thisTeam = allTeams[teamindex-1];
                var thisGame = allGames[allGames.length-1];
                if (oneprob > 0) {
                    thisGame.teams.push(thisTeam);
                    // find this game's ancestors
                    var lastGame = thisTeam.probs.map(hasValue).lastIndexOf(1);
                    if (lastGame > -1) {
                        if (thisGame.prevGames.length == 0) {
                            thisGame.prevGames.push(allGames[lastGame]);
                        } else {
                            var addThis = true;
                            thisGame.prevGames.forEach(function (g) {
                                if (allGames[lastGame] == g) {
                                    addThis = false;
                                }
                            });
                            if (addThis) {
                                thisGame.prevGames.push(allGames[lastGame]);
                            }

                        }
                    }
                }                    
                thisTeam.probs[realrowindex] = oneprob;   
            }
        });
    }
}

function padWithSpace(num) {
    if (num < 10) {
        return " " + num.toString();
    } else {
        return num.toString();        
    }
}

// for debugging purposes
function printTree(node) {
    if (node.children.length > 0) {
        document.writeln(" { name = " + node.name + ",");
        document.write("   childen (" + node.children.length + ") = [<br>    ");
        node.children.forEach(function (c) {
            printTree(c);
            document.write(",");                
        });
        document.write("] } <br>");            
    } else {
        document.writeln("{ name = " + node.name + "}");
    }

}

function teamover(t) {
    d3.select(this).selectAll("text")
        .transition().duration(100).attr("fill","#000");    
    
    svg.selectAll("g.futuregame").select("circle")
        .transition().duration(100)
        .attr("r",5)   // this is in case you roll from one team to next w/o activating teamout
        .style("fill","#ccc").style("stroke","#ccc");

    t.probs.forEach(function (p, gameindex) {
        if (p>0) {
            if (allGames[gameindex].played==0) {
            svg.selectAll("g.g"+gameindex).select("circle")
                .transition().duration(100)
                .attr("r",10)
                .style("fill","#99c").style("stroke","#000");
            svg.selectAll("g.g"+gameindex).append("text")
                .text((Math.round(p*1000)/10)+"%")
                .attr("class", "prob")
                .attr("x", 0)
                .attr("y", -15)
                .attr("text-anchor", "middle")
                .attr("transform", function(z) {return "rotate(" + (90-z.x) + ")"; })
                .attr("fill","#11f")
                .attr("font-size","0.9em");  
            }
        }        
    })
    
}
function teamout(t,i){
    d3.select(this).selectAll("text")
        .transition().duration(100).attr("fill","#aaa");
    
    svg.selectAll("g.futuregame").select("circle")
        .transition().duration(100)
        .attr("r",5)
        .style("fill","#99c").style("stroke","#99c");
    
    t.probs.forEach(function (p, gameindex) {
        if (p>0) { 
            svg.selectAll("g.g"+gameindex).select("text.prob").remove();
            
        }   
    });

}

function gameover(d, i) {
    svg.selectAll("g.futuregame").select("circle")
        .transition().duration(100)
        .attr("r",5)   // this is in case you roll from one team to next w/o activating teamout
        .style("fill","#ccc").style("stroke","#ccc");

    d3.select(this)
    .transition().duration(100)
    .attr("r",10)
    .style("stroke", "black").style("stroke-width", "2px");   

    d.teams.forEach(function (t) {
        if (t.stillin==1) {
            var nameText = svg.selectAll("g."+t.name.toLowerCase().replace(/\W/g,"_"))
                .selectAll("text")
                .transition().duration(100).attr("fill", "#000");
            svg.selectAll("g."+t.name.toLowerCase().replace(/\W/g,"_"))
                .append("text")
                .attr("class", "prob")
                .text((Math.round(t.probs[d.name]*1000)/10)+"%")
                .attr("x", function(d) { return d.x < 180 ? 10 : -10; })
                .attr("y",10)
                .attr("text-anchor", function(z) { return z.x < 180 ? "end" : "start"; })
                .attr("transform", function(z) { return z.x < 180 ? null : "rotate(180)"; })                
                .attr("fill","#11f")
                .attr("font-size","0.9em");
        }
    });       
}

function gameout(d, i) {
    svg.selectAll("g.futuregame").select("circle")
        .transition().duration(100)
        .attr("r",5)
        .style("fill","#99c").style("stroke","#99c");

    d3.select(this)
    .transition().duration(100)
    .attr("r",5)
    .style("stroke", "#99c").style("stroke-width", "2px");

    d.teams.forEach(function (t) {
        if (t.stillin ==1) {
            svg.selectAll("g."+t.name.toLowerCase().replace(/\W/g,"_"))
                    .selectAll("text")
                    .transition().duration(100).attr("fill", "#aaa");
            svg.selectAll("g."+t.name.toLowerCase().replace(/\W/g,"_"))
                    .select("text.prob").remove();
        }
    });       
}


// START MAIN JS STUFF
//
// these will hold the teams/games as they are being parsed from CSV
var allTeams = [];
var allGames = [];

// read in CSV file and parse each row
d3.text("bracket.csv", function(data) {        

    // save the CSV data as objects in allTeams and allGames
    var rows = d3.csv.parseRows(data);        
    rows.forEach(parseCSV); 

    // generate tree structure from the data that was read in
    var finalgame = allGames[allGames.length-1];
    var gameTree = new Object();
    gameTree.name = finalgame.name;
    gameTree.children = travelTree(finalgame);
    gameTree.teams = allTeams;
    gameTree.played = finalgame.played;

    // time to visualize!
    var tree = d3.layout.tree()
        .size([360, r])
        .separation(function(a, b) { 
            if (a.region != b.region) {
                return 1;
            } else {
                return (a.parent == b.parent ? 3 : 3) / a.depth; 
            }   
        });

    var diagonal = d3.svg.diagonal.radial()
        .projection(function(d) { return [d.y+5,  d.x / 180 * Math.PI]; });

    var nodes = tree.nodes(gameTree);
    var links = tree.links(nodes);

    var drawlink = svg.selectAll("path.link")
        .data(links)
        .enter()
        .append("path")
        .attr("class", "link")
        .attr("d", diagonal);

    var drawnode = svg.selectAll("g.node")
    .data(nodes)
    .enter().append("g")
    .attr("class","node")
    .attr("transform", function(d) { return "rotate(" + (d.x-90) + ")translate(" + d.y + ")"; })

    var playedGameNodes = drawnode.filter(function (d) {
        return ((typeof(d.name) == "number") && (d.played==1));       
    });

    var futureGameNodes = drawnode.filter(function (d) {
        return ((typeof(d.name) == "number") && (d.played==0));       
    });

    var teamInNodes = drawnode.filter(function (d) {
        return ((typeof(d.name) == "string") && (d.stillin==1));       
    });

    var teamOutNodes = drawnode.filter(function (d) {
        return ((typeof(d.name) == "string") && (d.stillin==0));       
    });
    
    var teamNodes = drawnode.filter(function (d) {
        return (typeof(d.name) == "string");       
    });

    playedGameNodes.attr("class", function(d) {return "node playedgame g" + d.name;})
        .append("circle").attr("r",3)
        .style("stroke", "#ccc").style("stroke-width", "2px")
        .style("fill", "#fff");

    futureGameNodes.attr("class", function(d) {return "node futuregame g" + d.name;})
        .append("circle").attr("r",5)            
        .style("stroke", "#99c").style("stroke-width", "2px")
        .style("fill", "#99c")
        .on("mouseover", gameover)
        .on("mouseout", gameout);

    teamNodes
    .attr("class", function (d) { return "node team " + d.name.toLowerCase().replace(/\W/g,"_"); })
    .append("text")
    .attr("x", function(d) { return d.x < 180 ? 40 : -40; })
    .attr("y", 10) 
    .attr("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
    .attr("transform", function(d) { return d.x < 180 ? null : "rotate(180)"; })
    .text(function(d) { return d.name; });
    
    teamNodes
    .append("text")
    .attr("x", function(d) { return d.x < 180 ? 25 : -25; })
    .attr("y", 10) 
    .attr("text-anchor", "middle")
    .attr("transform", function(d) { return d.x < 180 ? null : "rotate(180)"; })
    .text(function(d) { return d.seed; });
    
    teamInNodes.selectAll("text")
    .attr("fill", "#aaa");
        
    teamOutNodes.selectAll("text")
    .attr("fill", "#ddd").style("text-decoration","none");
    
    teamInNodes
    .on("mouseover", teamover)
    .on("mouseout", teamout);

});
