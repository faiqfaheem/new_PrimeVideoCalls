package com.axis.vpn.tools.prankvideocall.data.local;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class CustomCallerDao_Impl implements CustomCallerDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<CustomCallerEntity> __insertAdapterOfCustomCallerEntity;

  private final EntityDeleteOrUpdateAdapter<CustomCallerEntity> __deleteAdapterOfCustomCallerEntity;

  public CustomCallerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfCustomCallerEntity = new EntityInsertAdapter<CustomCallerEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `custom_callers` (`id`,`name`,`imagePath`,`videoUri`,`audioUri`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final CustomCallerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindText(2, entity.getName());
        statement.bindText(3, entity.getImagePath());
        statement.bindText(4, entity.getVideoUri());
        statement.bindText(5, entity.getAudioUri());
      }
    };
    this.__deleteAdapterOfCustomCallerEntity = new EntityDeleteOrUpdateAdapter<CustomCallerEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `custom_callers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final CustomCallerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final CustomCallerEntity caller,
      final Continuation<? super Unit> $completion) {
    if (caller == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfCustomCallerEntity.insert(_connection, caller);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object delete(final CustomCallerEntity caller,
      final Continuation<? super Unit> $completion) {
    if (caller == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfCustomCallerEntity.handle(_connection, caller);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<CustomCallerEntity>> getAll() {
    final String _sql = "SELECT * FROM custom_callers ORDER BY id DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"custom_callers"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfImagePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "imagePath");
        final int _columnIndexOfVideoUri = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "videoUri");
        final int _columnIndexOfAudioUri = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "audioUri");
        final List<CustomCallerEntity> _result = new ArrayList<CustomCallerEntity>();
        while (_stmt.step()) {
          final CustomCallerEntity _item;
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          final String _tmpName;
          _tmpName = _stmt.getText(_columnIndexOfName);
          final String _tmpImagePath;
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath);
          final String _tmpVideoUri;
          _tmpVideoUri = _stmt.getText(_columnIndexOfVideoUri);
          final String _tmpAudioUri;
          _tmpAudioUri = _stmt.getText(_columnIndexOfAudioUri);
          _item = new CustomCallerEntity(_tmpId,_tmpName,_tmpImagePath,_tmpVideoUri,_tmpAudioUri);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
