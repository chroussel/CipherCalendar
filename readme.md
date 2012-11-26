Cipher Calendar
---
We want to create a calendar which cannot be readable by people who would acces to google database or google account.
Use Google as a way to backup data.

---
Threat model : 
I Google server does not see the plain data. 
Threat : Big Brother, 

II May be use by other calendar using the calendar API.
Threat : unknown security from other calendar.

III May protect against phone steal
read data about calendars in the phone


---
Cipher Problem
Current : AES/CBC/PKS5Padding

Problem : How to transmit IVs ?
Current solution : simple but not the best : concat IV with ciphered bytes.
P : plain text
c() : cipher fonction
S : cipher text
M : message
K : secret key
IV : Initialisation vector (16 bytes)

coder : 
M = base64.code(c(P,K,IV)+IV)

decoder : 
Tmp = base64.decode(M)
IV = Tmp[Tmp.length-16..Tmp.length]
S = Tmp[0..Tmp.length-16]
P = c^{-1}(S,K,IV)

---
Implementation Choice : 

I Interface vs Sync Application

II Backup sync

To synchronize calendar and Google calendar, I use a database to store events ID
What is the default policy when there are some modifications. 

Modification rules : 

Data on google, no data in calendar, no data in database : pull and decrypt
Data on google, no data in calendar, data in database : (suppressed event) suppress event on google and event in db
Data on google, data in calendar, data in database : do nothing or push data in calendar
Data on google, data in calendar, no data in database : error

no data on google, no data in calendar, no data in database : nothing
no data on google, no data in calendar, data in database : suppress event

no data on google, data in calendar, any : push data to google

---
Architecture : 

