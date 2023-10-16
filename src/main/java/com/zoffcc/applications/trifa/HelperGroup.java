package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.GroupMessage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename;
import static com.zoffcc.applications.trifa.HelperFiletransfer.save_group_incoming_file;
import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.*;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILENAME_LENGTH;

public class HelperGroup {

    private static final String TAG = "trifa.Hlp.Group";

    public static byte[] hex_to_bytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static long tox_group_by_groupid__wrapper(String group_id_string)
    {
        ByteBuffer group_id_buffer = ByteBuffer.allocateDirect(GROUP_ID_LENGTH);
        byte[] data = hex_to_bytes(group_id_string.toUpperCase());
        group_id_buffer.put(data);
        group_id_buffer.rewind();

        long res = tox_group_by_chat_id(group_id_buffer);
        if (res == UINT32_MAX_JAVA)
        {
            return -1;
        }
        else if (res < 0)
        {
            return -1;
        }
        else
        {
            return res;
        }
    }

    public static String tox_group_by_groupnum__wrapper(long groupnum)
    {
        try
        {
            ByteBuffer groupid_buf = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);
            if (tox_group_get_chat_id(groupnum, groupid_buf) == 0)
            {
                byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                groupid_buf.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                return bytes_to_hex(groupid_buffer).toLowerCase();
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytes_to_hex(byte[] in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();

            for (byte b : in)
            {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    public static String fourbytes_of_long_to_hex(final long in)
    {
        return String.format("%08x", in);
    }

    public static String bytebuffer_to_hexstring(ByteBuffer in, boolean upper_case)
    {
        try
        {
            in.rewind();
            StringBuilder sb = new StringBuilder("");
            while (in.hasRemaining())
            {
                if (upper_case)
                {
                    sb.append(String.format("%02X", in.get()));
                }
                else
                {
                    sb.append(String.format("%02x", in.get()));
                }
            }
            in.rewind();
            return sb.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static class incoming_group_file_meta_data
    {
        String message_text;
        String path_name;
        String file_name;
    }

    static incoming_group_file_meta_data handle_incoming_group_file(long group_number, long peer_id, byte[] data, long length, long header)
    {
        incoming_group_file_meta_data ret = new incoming_group_file_meta_data();
        ret.message_text = null;
        ret.path_name = null;
        ret.file_name = null;
        try
        {
            long res = tox_group_self_get_peer_id(group_number);
            if (res == peer_id)
            {
                // HINT: do not add our own messages, they are already in the DB!
                Log.i(TAG, "group_custom_packet_cb:gn=" + group_number + " peerid=" + peer_id + " ignoring own file");
                return null;
            }

            String group_id = "-1";
            try
            {
                group_id = tox_group_by_groupnum__wrapper(group_number);
            }
            catch (Exception ignored)
            {
            }

            if (group_id.compareTo("-1") == 0)
            {
                return null;
            }


            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            hash_bytes.put(data, 8, 32);

            // TODO: fix me!
            long timestamp_unused = ((byte)data[8+32]<<3) + ((byte)data[8+32+1]<<2) + ((byte)data[8+32+2]<<1) + (byte)data[8+32+3];

            ByteBuffer filename_bytes = ByteBuffer.allocateDirect(TOX_MAX_FILENAME_LENGTH);
            filename_bytes.put(data, 8 + 32 + 4, 255);
            filename_bytes.rewind();
            String filename = "image.jpg";
            try
            {
                byte[] filename_byte_buf = new byte[255];
                Arrays.fill(filename_byte_buf, (byte)0x0);
                filename_bytes.rewind();
                filename_bytes.get(filename_byte_buf);

                int start_index = 0;
                int end_index = 254;
                for(int j=0;j<255;j++)
                {
                    if (filename_byte_buf[j] == 0)
                    {
                        start_index = j+1;
                    }
                    else
                    {
                        break;
                    }
                }

                for(int j=254;j>=0;j--)
                {
                    if (filename_byte_buf[j] == 0)
                    {
                        end_index = j;
                    }
                    else
                    {
                        break;
                    }
                }

                byte[] filename_byte_buf_stripped = Arrays.copyOfRange(filename_byte_buf,start_index,end_index);
                filename = new String(filename_byte_buf_stripped, StandardCharsets.UTF_8);
                //Log.i(TAG,"group_custom_packet_cb:filename str=" + filename);

                //Log.i(TAG, "group_custom_packet_cb:filename:"+filename_bytes.arrayOffset()+" "
                //+filename_bytes.limit()+" "+filename_bytes.array().length);
                //Log.i(TAG, "group_custom_packet_cb:filename hex="
                //           + HelperGeneric.bytesToHex(filename_bytes.array(),filename_bytes.arrayOffset(),filename_bytes.limit()));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            long file_size = length - header;
            if (file_size < 1)
            {
                Log.i(TAG, "group_custom_packet_cb: file size less than 1 byte");
                return null;
            }

            String filename_corrected = get_incoming_filetransfer_local_filename(filename, group_id.toLowerCase());

            String tox_peerpk = tox_group_peer_get_public_key(group_number, peer_id).toUpperCase();
            String peername = tox_group_peer_get_name(group_number, peer_id);
            long timestamp = System.currentTimeMillis();

            GroupMessage m = new GroupMessage();
            m.is_new = false;
            m.tox_group_peer_pubkey = tox_peerpk;
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.read = false;
            m.tox_group_peername = peername;
            m.private_message = 0;
            m.group_identifier = group_id.toLowerCase();
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.rcvd_timestamp = timestamp;
            m.sent_timestamp = timestamp;
            m.text = filename_corrected + "\n" + file_size + " bytes";
            m.message_id_tox = "";
            m.was_synced = false;
            m.path_name = VFS_FILE_DIR + "/" + m.group_identifier + "/";
            m.file_name = filename_corrected;
            m.filename_fullpath = m.path_name + m.file_name;
            m.msg_id_hash = bytebuffer_to_hexstring(hash_bytes, true);
            m.filesize = file_size;

            File f1 = new File(m.path_name + "/" + m.file_name);
            File f2 = new File(f1.getParent());
            f2.mkdirs();

            save_group_incoming_file(m.path_name, m.file_name, data, header, file_size);
            long row_id = -1;
            try
            {
                row_id = TrifaToxService.Companion.getOrma().insertIntoGroupMessage(m);
            } catch (Exception e)
            {
            }

            ret.message_text =  m.text;
            ret.path_name = m.path_name;
            ret.file_name = m.file_name;

            return ret; // return metadata
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}