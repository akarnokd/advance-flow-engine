
function myclick(node) {
    d3.selectAll("path.link")
    .transition().duration(200)
    .style("stroke-width", "1.5px")
    .style("stroke", "#ccc");

    d3.selectAll("g.node_sel")
    .transition().duration(200)
    .style("opacity", 0)

    if (!node.parent) return;
    
    while (node) {
        if (node.linkToParent) {
            d3.select("path[id="+node.linkToParent+"]")
            .transition().duration(400)
            .style("stroke-width", "3px")
            .style("stroke", "#317834");
        }
    
        d3.selectAll("g.node_sel[id="+node.rectId+"]")
        .transition().duration(400)
        .style("opacity", 1);

        node = node.parent;
    }
}

var linkId = 0;
var makeLinkId = function(link) {
    var name = "link" + (++linkId);
    link.target.linkToParent = name;
    return name;
}
var makeRectId = function(node) {
    var name = "rect" + (++linkId);
    node.rectId = name;
    return name;
}

d3.json("../FileViewData?fileName="+filename, function(json) {
    var tree = d3.layout.tree();
    var nodes = tree.nodes(json);

    var max_depth = d3.max(nodes, function(n) {return n.depth;});

    var width = 960;
    var height = 60 * max_depth;

    var diagonal = d3.svg.diagonal()
        .projection(function(d) {return [d.x*width, d.y*height];});

    var svg = d3.select("#tree")
        .append("svg")
        //.style("background","#eee") // <- debug
        .attr("width", width)
        .attr("height", height+60);
    
    var grad = svg.append("linearGradient")
        .attr("id", "gradient")
         .attr("x1", "50%")
         .attr("y1", "0%")
         .attr("x2", "50%")
         .attr("y1", "100%");
        
    grad.append("stop")
        .attr("offset", "0%")
        .attr("stop-color", "#388d3c");
        
    grad.append("stop")
        .attr("offset", "100%")
        .attr("stop-color", "white");
        
    var vis = svg.append("g")
        .attr("transform", "translate(0, 30)");

    var link = vis.selectAll("path.link")
    .data(tree.links(nodes))
    .enter().append("path")
    .attr("id", makeLinkId)
    .attr("class", "link")
    .attr("d", diagonal);

    var node = vis.selectAll("g.node").data(nodes).enter().append("g")
    .attr("class", "node")
    .attr("id", makeRectId)
    .attr("transform", function(d) {return "translate(" + d.x*width + "," + d.y*height + ")";});

    var charwidth = 6;
    
    //node.append("circle")
    //.attr("r", 4)
    //.attr("transform", function(d) {return "translate(0," + (d.children ? 0 : yoffset) + ")";});

    node.append("rect")    
    .attr("x", function(d) {return d.name.length * charwidth * -0.5;})
    .attr("y", -14)
    .attr("rx", 10)
    .attr("ry", 10)
    .attr("width", function(d) {return d.name.length * charwidth;})
    .attr("height", 20)
    .attr("class", "node");

    node.append("text")
    .attr("text-anchor", "middle")
    .attr("class", "node")
    .text(function(d) {return d.name;});

    node = vis.selectAll("g.node_sel").data(nodes).enter().append("g")
    .attr("class", "node_sel")
    .attr("id", function(d) { return d.rectId; })
    .attr("cursor", "pointer")
    .attr("transform", function(d) {return "translate(" + d.x*width + "," + d.y*height + ")";})
    .on("click", myclick)
    .on("mouseover", function(node) {             
            d3.selectAll("g.node[id="+node.rectId+"] > rect")
            .style("stroke", "black");
    })
    .on("mouseout", function(node) { d3.selectAll("g.node[id="+node.rectId+"] > rect").style("stroke", "#ccc")})
    .style("opacity", 0);
    
    node.append("rect")    
    .attr("x", function(d) {return d.name.length * charwidth * -0.5;})
    .attr("y", -14)
    .attr("rx", 10)
    .attr("ry", 10)
    .attr("width", function(d) {return d.name.length * charwidth;})
    .attr("height", 20)    
    .attr("class", "node_sel");

    node.append("text")
    .attr("text-anchor", "middle")
    .attr("class", "node_sel")
    .text(function(d) {return d.name;});
});