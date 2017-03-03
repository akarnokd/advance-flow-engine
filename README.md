# advance-flow-engine
Software of the EU project ADVANCE: Advanced predictive-analysis-based decision-support engine for logistics

http://advance-logistics.eu

  - Advance Flow Engine: the first reactive, dynamic, block-oriented dataflow processing framework on the JVM
    - Flow Engine API in Java & C# to help interoperate between engines and/or external services via streaming XML
    - The novel XML type system, that allows covariant service composition over XML schema-defined datasources and sinks
    - The novel (pluggable) type-inference system on top of the XML type system
    - The novel transformation from a flow-graph described via XML into actual reactive-Observable based flow runtime
  - Advance Flow Editor: visual editor for designing flow-graphs, built in run and inspection features
  - Advance Elicitation Tool: can't remember the purpose of this :(
  - Advance Live Reporter: real-time tracking of inventory via a web application for a hub-and-spoke logistics environment
    - Includes some machine learning for predicting parcel amounts at various times of the day
    - Tablet friendly real-time status feedback
