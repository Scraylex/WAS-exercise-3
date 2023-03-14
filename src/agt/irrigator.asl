// irrigator_agent

repo_location("https://sandbox-graphdb.interactions.ics.unisg.ch/repositories/was-exercise-3-lukas").

!start.

+!start : repo_location(KGRepo) <-
    .print("Hello world");
    !setup_artifacts(KGRepo).

+!setup_artifacts(KGRepo) : true <-
    makeArtifact("farmKG2", "farm.FarmKG", [KGRepo], FarmKGId); 

    queryFarm(Farm);
    +farm(Farm);
    .print("Queried farm to manage: ", Farm);

    queryThing(Farm, "https://was-course.interactions.ics.unisg.ch/farm-ontology#IrrigateAffordance", ThingDescription);
    .print("Queried td:Thing for irrigating: ", ThingDescription);
    makeArtifact("tractor2", "wot.ThingArtifact", [ThingDescription], TractorId).

+low_moisture(Coordinates)[source(Agent)] : true <-
    .print(Agent, " claims low moisture in: ", Coordinates);

    !irrigate(Coordinates).

+!irrigate(Coordinates) : low_moisture(Coordinates)[source(Agent)] <-
    .print("Irrigating ", Coordinates);
    invokeAction("https://was-course.interactions.ics.unisg.ch/farm-ontology#IrrigateAffordance", ["https://www.w3.org/2019/wot/json-schema#ArraySchema"], Coordinates)[artifact_id(TractorId)];
    -low_moisture(Coordinates)[source(Agent)].


