package lx.edu.springboot.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    public List<Map<String, Object>> getKidsByParentUid(String parentUid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("kidinformation")
                .whereEqualTo("parentUid", parentUid)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Map<String, Object>> kids = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            kids.add(data);
        }

        return kids;
    }

    public Map<String, Object> getKidDetail(String parentUid, String kidName) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("kidinformation")
                .whereEqualTo("parentUid", parentUid)
                .whereEqualTo("kid", kidName)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) return null;

        Map<String, Object> data = documents.get(0).getData();
        data.put("id", documents.get(0).getId());
        return data;
    }

}
