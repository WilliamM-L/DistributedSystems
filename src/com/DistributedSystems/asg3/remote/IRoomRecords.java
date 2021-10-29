package com.DistributedSystems.asg3.remote;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.time.LocalDate;

@WebService
@SOAPBinding(style = Style.RPC)
public interface IRoomRecords {
    @WebMethod
    String createRoom(int room_Number, String dateText, String timeSlotListText, String userID);
    @WebMethod
    String deleteRoom (int roomNumber, String dateText, String timeSlotListText, String userID);
    @WebMethod
    String bookRoom(String campusName, int roomNumber, String dateText, String timeSlotText, String userID);
    @WebMethod
    String getAvailableTimeSlot(String dateText, String userID);
    @WebMethod
    String cancelBooking(String bookingID, String userID);
    @WebMethod
    String changeReservation(String bookingID, String newCampusName, int newRoomNum, String newTimeSlotText, String newDateText, String userID);
}
