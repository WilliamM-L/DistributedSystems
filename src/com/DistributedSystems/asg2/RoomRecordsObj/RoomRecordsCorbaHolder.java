package com.DistributedSystems.asg2.RoomRecordsObj;

/**
* RoomRecordsObj/RoomRecordsCorbaHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RoomRecords.idl
* Monday, October 18, 2021 9:05:05 o'clock PM EDT
*/

public final class RoomRecordsCorbaHolder implements org.omg.CORBA.portable.Streamable
{
  public RoomRecordsCorba value = null;

  public RoomRecordsCorbaHolder ()
  {
  }

  public RoomRecordsCorbaHolder (RoomRecordsCorba initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RoomRecordsCorbaHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RoomRecordsCorbaHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RoomRecordsCorbaHelper.type ();
  }

}
