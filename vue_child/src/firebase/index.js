import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
// 'firebase/firestore'에서 getFirestore를 가져옵니다.
import { getFirestore,collection } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyAG-rJyK3DGX3kGG_fLjigklBKKBBnkSOg",
  authDomain: "child119-5fe1f.firebaseapp.com",
  projectId: "child119-5fe1f",
  storageBucket: "child119-5fe1f.firebasestorage.app",
  messagingSenderId: "337805756575",
  appId: "1:337805756575:web:6fbdea6e400ff2d68a3aaa",
  measurementId: "337805756575"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const USER_COLLECTION = collection(db, "users");