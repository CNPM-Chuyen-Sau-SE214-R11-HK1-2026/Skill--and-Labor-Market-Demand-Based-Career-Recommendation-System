import React from 'react';
import { Link } from 'react-router-dom';

export const NotFoundPage: React.FC = () => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        flex: 1,
        padding: '3rem 1rem',
        textAlign: 'center',
      }}
    >
      <h1 style={{ fontSize: '4rem', fontWeight: 800, color: 'var(--color-primary)' }}>404</h1>
      <h2 style={{ fontSize: '1.5rem', marginBottom: '1rem', color: 'var(--color-text-main)' }}>
        Trang không tồn tại
      </h2>
      <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>
        Đường dẫn bạn yêu cầu không tìm thấy trên hệ thống.
      </p>
      <Link
        to="/"
        style={{
          padding: '0.5rem 1.5rem',
          backgroundColor: 'var(--color-primary)',
          color: '#ffffff',
          borderRadius: 'var(--radius-md)',
          textDecoration: 'none',
          fontWeight: 600,
        }}
      >
        Về trang chủ
      </Link>
    </div>
  );
};
