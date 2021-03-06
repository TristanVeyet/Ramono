package com.TriVe.Apps.mycontact.ContactAPI;

import java.util.ArrayList;
import java.util.List;

import com.TriVe.Apps.mycontact.ContactAPI.objects.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.content.ContentResolver;


/**
 * <b>Contact management API.</b>
 *
 * @author TriVe
 * @version 1.0
 */
public class ContactAPI
{
    /**
     * Used to query the device database
     */
    private ContentResolver cr;

    /**
     * Needed by the ContentResolver
     */
    private Context context;

    /**
     * ContactAPI constructor
     * <p>
     * Used to get the caller context used by the ContentResolver.
     * </p>
     * @param context Caller context
     */
    public ContactAPI(Context context)
    {
        this.context = context;
    }

    /**
     * Create and return the contact list.
     * @param context Caller context
     * @return The contact list in the device
     */
    public static List<Contact> newContactList(Context context)
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        String id;
        Cursor cur;
        ContentResolver cr;


        cr = context.getContentResolver();

        cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Contact c = new Contact();
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                c.setId(id);
                c.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                contacts.add(c);
            }
        }

        cur.close();
        return contacts;
    }

    /**
     * Get contact data  from contact id
     * @param id Contact id
     * @return The contact
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Contact GetContactInfoFromID(String id)
    {
        String dbID;
        Contact c = new Contact();

        this.cr = this.context.getContentResolver();

        Cursor cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                dbID = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                if (dbID.equals(id))
                {
                    c.setId(id);
                    c.setRawId(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)));
                    c.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                    {
                        c.setPhone(this.getPhoneNumbers(id));
                    }
                    c.setEmail(this.getEmailAddresses(id));
                    c.setNotes(this.getContactNotes(id));
                    c.setAddresses(this.getContactAddresses(id));
                    return c;
                }
            }
        }

        cur.close();
        return null;
    }

    /**
     * Get phone numbers from contact id
     * @param id Contact id
     * @return List of phone numbers
     */
    public List<Phone> getPhoneNumbers(String id)
    {
        List<Phone> phones = new ArrayList<>();

        Cursor pCur = this.cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                new String[]{id}, null);
        while (pCur.moveToNext()) {
            phones.add(new Phone(
                    pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    , pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                    , pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL))
            ));

        }
        pCur.close();
        return(phones);
    }

    /**
     * Get Email addresses from contact id
     * @param id Contact id
     * @return List of Email addresses
     */
    public List<Email> getEmailAddresses(String id) {
        List<Email> emails = new ArrayList<>();

        Cursor emailCur = this.cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id}, null);
        while (emailCur.moveToNext()) {
            // This would allow you get several email addresses
            String DATA = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            String TYPE = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            String LABEL = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
            Email e = new Email(DATA,TYPE, LABEL);
            emails.add(e);
        }
        emailCur.close();
        return(emails);
    }

    /**
     * Get Notes from contact id
     * @param id Contact id
     * @return List of Notes Strings
     */
    public List<String> getContactNotes(String id) {
        List<String> notes = new ArrayList<>();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        Cursor noteCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
        if (noteCur.moveToFirst()) {
            String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            if (note.length() > 0) {
                notes.add(note);
            }
        }
        noteCur.close();
        return(notes);
    }

    /**
     * Get the postal addresses from contact id
     * @param id Contact id
     * @return List of addresses
     */
    public List<Address> getContactAddresses(String id) {
        List<Address> addrList = new ArrayList<>();

        this.cr = this.context.getContentResolver();

        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor addrCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
        while(addrCur.moveToNext()) {
            String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
            String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            String label = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.LABEL));
            Address a = new Address(poBox, street, city, state, postalCode, country, type, label);
            addrList.add(a);
        }
        addrCur.close();
        return(addrList);
    }

}