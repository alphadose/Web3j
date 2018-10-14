package com.example.alphadose.ether;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    public static final String endpoint = "http://10.42.0.1:8545";
    private static final String privkey = "0x3d609b4857e397a3c10b2769ba60eb6f32aa69b3df8e645c465409bd81398fe0";

    Web3j web3j;
    Credentials credentials;
    EditText token;

    public void transfer(String address) {
        TransactionReceipt transferReceipt;
        try {
            transferReceipt = Transfer.sendFunds(
                    web3j, credentials, address,
                    BigDecimal.valueOf(1.0), Convert.Unit.ETHER)
                    .send();
            Log.i("TRANSACTION","Transaction complete "
                    + transferReceipt.getTransactionHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBalance() {
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(
                    credentials.getAddress(),
                    DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();

            BigDecimal temp = Convert.fromWei(String.valueOf(ethGetBalance.getBalance()), Convert.Unit.ETHER);
            String ether = String.valueOf(temp.setScale(0, RoundingMode.HALF_UP));

            return ether;

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    public class TransferAndUpdate extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            transfer("0xf5116e96F4D9148F3fD882625e4Af254d055AD37");
            return getBalance();
        }

        @Override
        protected void onPostExecute( final String ether ) {
            token.setText(ether);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Update extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            return getBalance();
        }

        @Override
        protected void onPostExecute( final String ether ) {
            token.setText(ether);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button get = findViewById(R.id.get);
        Button send = findViewById(R.id.send);
        token = findViewById(R.id.token);

        web3j = Web3jFactory.build(new HttpService(endpoint));
//      credentials = WalletUtils.loadCredentials("",  "/path/to/walletfile");
        credentials = Credentials.create(privkey);

        Log.i("CREDENTIALS","Credentials loaded");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 new TransferAndUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
        });

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Update().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
        });
    }

}
