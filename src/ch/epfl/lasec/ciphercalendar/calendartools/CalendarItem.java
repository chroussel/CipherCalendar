package ch.epfl.lasec.ciphercalendar.calendartools;

public class CalendarItem {

    public String id;
    public String calendarName;
    public String accountName;
    public String accountType;
    public int color;

    public CalendarItem(String id, String calendarName, String accountName,
	    String accountType, int color) {
	this.id = id;
	this.calendarName = calendarName;
	this.accountName = accountName;
	this.accountType = accountType;
	this.color = color;
    }

    @Override
    public String toString() {
	return calendarName + "\n" + accountName;
    }

    public String details() {
	return "Calendar ID: " + id + "\n" + "Calendar Name: " + calendarName
		+ "\n" + "From account: " + accountName + "\n" + "With type: "
		+ accountType;
    }

    public boolean isSync() {
	return CalendarContent.CIPHER_ITEMS.contains(calendarName);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((accountName == null) ? 0 : accountName.hashCode());
	result = prime * result
		+ ((accountType == null) ? 0 : accountType.hashCode());
	result = prime * result
		+ ((calendarName == null) ? 0 : calendarName.hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	CalendarItem other = (CalendarItem) obj;
	if (accountName == null) {
	    if (other.accountName != null)
		return false;
	} else if (!accountName.equals(other.accountName))
	    return false;
	if (accountType == null) {
	    if (other.accountType != null)
		return false;
	} else if (!accountType.equals(other.accountType))
	    return false;
	if (calendarName == null) {
	    if (other.calendarName != null)
		return false;
	} else if (!calendarName.equals(other.calendarName))
	    return false;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	return true;
    }

}