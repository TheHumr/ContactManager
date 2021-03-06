package contactorg.thehumr.cz.contactorganizer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MyActivity extends Activity {
    // some changes
    private static final int EDIT = 0, DELETE = 1;
    public static final String SHAREDPREFERENCES_SET = "contactPrefs";
    public static final String SHARED_PREF_CONTACT_NAME = "ContactName";
    public static final String SHARED_PREF_CONTACT_TIME = "ContactTime";


    EditText txtName, txtAdress, txtEmail, txtPhone;
    ImageView imageViewContactImg;
    List<Contact> contacts = new ArrayList<Contact>();
    ListView contactListView;
    Button btnAdd;
    Uri imageURI = Uri.parse("android.resource://contactorg.thehumr.cz.contactorganizer/drawable/nouser.jpg");
    DatabaseHandler dbHandler;
    int longClickedItemIndex;
    ArrayAdapter<Contact> contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        txtName = (EditText) findViewById(R.id.txtName);
        txtAdress = (EditText) findViewById(R.id.txtAdress);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPhone = (EditText) findViewById(R.id.txtPhone);

        contactListView = (ListView) findViewById(R.id.listView);

        dbHandler = new DatabaseHandler(getApplicationContext());

        registerForContextMenu(contactListView);
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longClickedItemIndex = i;
                return false;
            }
        });

        setupTabHost();
        setupAddButton();

        displayLastAddedContact();


        imageViewContactImg = (ImageView) findViewById(R.id.imgViewContactImage);
        imageViewContactImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), 1);
            }
        });

        if (dbHandler.getContactsCount() != 0)
            contacts.addAll(dbHandler.getAllContacts());
        populateList();
    }

    private void displayLastAddedContact() {
        String lastAddedContact = getLastAddedContact();
        String lastAddedContactTime = getLastAddedContactTime();
        String name = "Last added contact: " + lastAddedContact + ", " + lastAddedContactTime;

        TextView textViewLastAddedContact = (TextView)findViewById(R.id.txtLastAddedContact);
        textViewLastAddedContact.setText(name);
    }


    private String getLastAddedContact() {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFERENCES_SET, MODE_PRIVATE);
        String extractedText = prefs.getString(SHARED_PREF_CONTACT_NAME, "No contact recorded");
        return extractedText;
    }

    private String getLastAddedContactTime() {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFERENCES_SET, MODE_PRIVATE);
        String extractedText = prefs.getString(SHARED_PREF_CONTACT_TIME, "");
        return extractedText;
    }

    private void storeLastAddedContactToSharedPreferences(Contact contact){
        String name = contact.getName();
        String time = new Date().toString();

        SharedPreferences prefs = getSharedPreferences(SHAREDPREFERENCES_SET, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREF_CONTACT_NAME, name);
        editor.putString(SHARED_PREF_CONTACT_TIME, time);
        editor.commit();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("Contact Options");
        menu.add(Menu.NONE, EDIT, menu.NONE, "Edit Contact");
        menu.add(Menu.NONE, DELETE, menu.NONE, "Delete Contact");
    }

    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case EDIT:
                // TODO editing a contact
                break;
            case DELETE:
                dbHandler.deleteContact(contacts.get(longClickedItemIndex));
                contacts.remove(longClickedItemIndex);
                contactAdapter.notifyDataSetChanged();
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void setupAddButton() {
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact contact = new Contact(dbHandler.getContactsCount(), String.valueOf(txtName.getText()), String.valueOf(txtPhone.getText()), String.valueOf(txtEmail.getText()), String.valueOf(txtAdress.getText()), imageURI);
                if (!contactExists(contact)){
                    dbHandler.createContact(contact);
                    contacts.add(contact);
                    contactAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), String.valueOf(txtName.getText()) +  " has been added to your Contacts!",Toast.LENGTH_SHORT).show();

                    storeLastAddedContactToSharedPreferences(contact);
                    displayLastAddedContact();

                    return;
                }
                Toast.makeText(getApplicationContext(), String.valueOf(txtName.getText()) + " already exists. Please use a different name.", Toast.LENGTH_SHORT).show();
            }
        });

        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                btnAdd.setEnabled(String.valueOf(txtName.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setupTabHost() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);
    }

    private boolean contactExists(Contact contact){
        String name = contact.getName();
        int contactCount = contacts.size();

        for (int i = 0; i< contactCount; i++){
            if (name.compareToIgnoreCase(contacts.get(i).getName()) == 0){
                return true;
            }
        }
        return false;
    }

    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1) {
                imageURI = data.getData();
                imageViewContactImg.setImageURI(data.getData());
            }
        }
    }

    private void populateList(){
        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }

    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter(){
            super(MyActivity.this, R.layout.listviewitem, contacts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listviewitem, parent, false);
            }

            Contact currentContact = contacts.get(position);

            TextView name = (TextView) convertView.findViewById(R.id.contactName);
            name.setText(currentContact.getName());
            TextView phone = (TextView) convertView.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());
            TextView email = (TextView) convertView.findViewById(R.id.email);
            email.setText(currentContact.getEmail());
            TextView address = (TextView) convertView.findViewById(R.id.address);
            address.setText(currentContact.getAddress());
            ImageView ivContactImage = (ImageView) convertView.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.getImageURI());

            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
