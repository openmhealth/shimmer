package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.PhysicalActivity;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/14/15.
 */
public class GoogleFitPhysicalActivityDataPointMapper extends GoogleFitDataPointMapper<PhysicalActivity> {

    protected ImmutableMap<Integer, String> googleFitDataTypes;
    protected ImmutableList<Integer> sleepActivityTypes;

    public GoogleFitPhysicalActivityDataPointMapper(){
        ImmutableMap.Builder<Integer, String> activityDataTypeBuilder = ImmutableMap.builder();
        activityDataTypeBuilder.put(9, "Aerobics")
                .put(10, "Badminton")
                .put(11, "Baseball")
                .put(12, "Basketball")
                .put(13, "Biathlon")
                .put(1, "Biking")
                .put(14, "Handbiking")
                .put(15, "Mountain biking")
                .put(16, "Road biking")
                .put(17, "Spinning")
                .put(18, "Stationary biking")
                .put(19, "Utility biking")
                .put(20, "Boxing")
                .put(21, "Calisthenics")
                .put(22, "Circuit training")
                .put(23, "Cricket")
                .put(106, "Curling")
                .put(24, "Dancing")
                .put(102, "Diving")
                .put(25, "Elliptical")
                .put(103, "Ergometer")
                .put(26, "Fencing")
                .put(27, "Football (American)")
                .put(28, "Football (Australian)")
                .put(29, "Football (Soccer)")
                .put(30, "Frisbee")
                .put(31, "Gardening")
                .put(32, "Golf")
                .put(33, "Gymnastics")
                .put(34, "Handball")
                .put(35, "Hiking")
                .put(36, "Hockey")
                .put(37, "Horseback riding")
                .put(38, "Housework")
                .put(104, "Ice skating")
                .put(0, "In vehicle")
                .put(39, "Jumping rope")
                .put(40, "Kayaking")
                .put(41, "Kettlebell training")
                .put(42, "Kickboxing")
                .put(43, "Kitesurfing")
                .put(44, "Martial arts")
                .put(45, "Meditation")
                .put(46, "Mixed martial arts")
                .put(2, "On foot")
                .put(47, "P90X exercises")
                .put(48, "Paragliding")
                .put(49, "Pilates")
                .put(50, "Polo")
                .put(51, "Racquetball")
                .put(52, "Rock climbing")
                .put(53, "Rowing")
                .put(54, "Rowing machine")
                .put(55, "Rugby")
                .put(8, "Running")
                .put(56, "Jogging")
                .put(57, "Running on sand")
                .put(58, "Running (treadmill)")
                .put(59, "Sailing")
                .put(60, "Scuba diving")
                .put(61, "Skateboarding")
                .put(62, "Skating")
                .put(63, "Cross skating")
                .put(105, "Indoor skating")
                .put(64, "Inline skating (rollerblading)")
                .put(65, "Skiing")
                .put(66, "Back-country skiing")
                .put(67, "Cross-country skiing")
                .put(68, "Downhill skiing")
                .put(69, "Kite skiing")
                .put(70, "Roller skiing")
                .put(71, "Sledding")
                .put(72, "Sleeping")
                .put(73, "Snowboarding")
                .put(74, "Snowmobile")
                .put(75, "Snowshoeing")
                .put(76, "Squash")
                .put(77, "Stair climbing")
                .put(78, "Stair-climbing machine")
                .put(79, "Stand-up paddleboarding")
                .put(3, "Still (not moving)")
                .put(80, "Strength training")
                .put(81, "Surfing")
                .put(82, "Swimming")
                .put(84, "Swimming (open water)")
                .put(83, "Swimming (swimming pool)")
                .put(85, "Table tenis (ping pong)")
                .put(86, "Team sports")
                .put(87, "Tennis")
                .put(5, "Tilting (sudden device gravity change)")
                .put(88, "Treadmill (walking or running)")
                .put(4, "Unknown (unable to detect activity)")
                .put(89, "Volleyball")
                .put(90, "Volleyball (beach)")
                .put(91, "Volleyball (indoor)")
                .put(92, "Wakeboarding")
                .put(7, "Walking")
                .put(93, "Walking (fitness)")
                .put(94, "Nording walking")
                .put(95, "Walking (treadmill)")
                .put(96, "Waterpolo")
                .put(97, "Weightlifting")
                .put(98, "Wheelchair")
                .put(99, "Windsurfing")
                .put(100, "Yoga")
                .put(101, "Zumba")
                .put(108, "Other")
                .put(109, "Light sleep")
                .put(110, "Deep sleep")
                .put(111, "REM sleep")
                .put(112, "Awake (during sleep cycle)");
        googleFitDataTypes = activityDataTypeBuilder.build();
        sleepActivityTypes = ImmutableList.of(72, 109, 110, 111, 112);
    }

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode listNode) {
        JsonNode listValueNode = asRequiredNode(listNode,"value");
        long activityTypeId = asRequiredLong(listValueNode.get(0),"intVal");

        //This means that the activity was actually sleep which should be captured using sleep duration
        if(sleepActivityTypes.contains((int)activityTypeId)){
            return Optional.empty();
        }

        String activityName = googleFitDataTypes.get((int)activityTypeId);
        PhysicalActivity.Builder physicalActivityBuilder = new PhysicalActivity.Builder(
                activityName);
        setEffectiveTimeFrameIfPresent(physicalActivityBuilder,listNode);
        PhysicalActivity physicalActivity = physicalActivityBuilder.build();
        Optional<String> originSourceId = asOptionalString(listNode, "originDataSourceId");
        return Optional.of(newDataPoint(physicalActivity,originSourceId.orElse(null)));

    }

}
