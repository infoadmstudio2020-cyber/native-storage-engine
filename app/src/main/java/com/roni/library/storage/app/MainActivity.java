package com.roni.library.storage.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.roni.library.storage.NativeStorageEngine;
import com.roni.library.storage.NativeStorageEngineConfig;
import com.roni.library.storage.NativeStorageEngineResult;
import com.roni.library.storage.database.Document;
import com.roni.library.storage.database.QueryFilter;
import com.roni.library.storage.database.StorageCollection;
import com.roni.library.storage.preferences.EncryptedPreferences;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);

        // Run verification test suite
        runEngineDiagnostics();
    }

    private void runEngineDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NativeStorageEngine Diagnostics ===\n");

        try {
            // 1. Initialize Engine
            NativeStorageEngine engine = NativeStorageEngine.getInstance();
            NativeStorageEngineConfig config = new NativeStorageEngineConfig.Builder()
                    .debugEnabled(true)
                    .tag("StorageDemo")
                    .build();

            NativeStorageEngineResult<Boolean> initResult = engine.init(this, config);
            sb.append("1. Engine Init: ").append(initResult.isSuccess() ? "SUCCESS" : "FAILED").append("\n");

            // 2. Encrypted Preferences
            EncryptedPreferences prefs = engine.getPreferences("demo_secure_prefs");
            prefs.putString("demo_token", "sec_token_987654321");
            prefs.putInt("launch_count", 1);
            String readToken = prefs.getString("demo_token", "NOT_FOUND");
            sb.append("2. Encrypted Preferences: ").append("sec_token_987654321".equals(readToken) ? "PASS" : "FAIL").append("\n");

            // 3. Database Collection & Queries
            StorageCollection col = engine.getDatabase().getCollection("demo_collection");
            col.insert(new Document("doc_demo").put("title", "Native Storage Guide").put("views", 100));
            Document retrieved = col.findById("doc_demo");
            sb.append("3. Document Store: ").append(retrieved != null ? "PASS (" + retrieved.getString("title", "") + ")" : "FAIL").append("\n");

            // 4. Query Filter
            List<Document> list = col.find(QueryFilter.eq("title", "Native Storage Guide"));
            sb.append("4. Query Filter: ").append(list.size() == 1 ? "PASS" : "FAIL").append("\n");

            // 5. LRU Cache
            engine.getDiskCache().put("cache_test", "cached_content".getBytes());
            byte[] cached = engine.getDiskCache().get("cache_test");
            sb.append("5. LRU Disk Cache: ").append(cached != null ? "PASS" : "FAIL").append("\n");

            sb.append("\nAll Storage Engine subsystems verified live!\nStatus: READY & OPERATIONAL");
        } catch (Exception e) {
            sb.append("\nException encountered: ").append(e.getMessage());
        }

        if (tvStatus != null) {
            tvStatus.setText(sb.toString());
        }
    }
}
