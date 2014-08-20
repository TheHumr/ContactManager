package contactorg.thehumr.cz.contactorganizer;

import android.net.Uri;

/**
 * Created by TheHumr on 11. 8. 2014.
 */
public class Contact {
    private String name, phone, email, address;
    private Uri imageURI;
    private int id;

    public Contact (int id, String name, String phone, String email, String address, Uri imageURI) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.imageURI = imageURI;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public Uri getImageURI() { return imageURI; }
}
