package playlist;

import javazoom.jl.player.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.util.ArrayList;

public class PlayList extends JFrame {
    private JTextField title, artist, album, year, search;
    private JButton btnAdd, btnDelete, btnSearch, btnPlay, btnNext, btnPrevious, btnToggleFavorite;
    private JTable musicTable;
    private DefaultTableModel musicTableModel;
    private ArrayList<Music> musicList = new ArrayList<>();
    private ArrayList<Music> displayedList = new ArrayList<>();
    private int currentIndex = -1;
    private JLabel statusLabel;
    private boolean isFavoriteMode = false;

    // mp3 재생
    private Player player;
    private Thread playThread;

    public PlayList() {
        setTitle("PlayList");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(0, 1));

        // 입력 패널
        JPanel inputPanel = new JPanel();
        title = new JTextField(10);
        artist = new JTextField(10);
        album = new JTextField(10);
        year = new JTextField(10);
        btnAdd = new JButton("등록");

        inputPanel.add(new JLabel("제목"));
        inputPanel.add(title);
        inputPanel.add(new JLabel("아티스트"));
        inputPanel.add(artist);
        inputPanel.add(new JLabel("앨범"));
        inputPanel.add(album);
        inputPanel.add(new JLabel("발매일"));
        inputPanel.add(year);
        inputPanel.add(btnAdd);

        // 검색 패널
        JPanel searchPanel = new JPanel();
        search = new JTextField(15);
        btnSearch = new JButton("🔍");
        btnDelete = new JButton("선택 삭제");
        btnToggleFavorite = new JButton("즐겨찾기 보기");
        statusLabel = new JLabel("전체 목록 (0곡)");

        searchPanel.add(statusLabel);
        searchPanel.add(new JLabel("음악 검색"));
        searchPanel.add(search);
        searchPanel.add(btnSearch);
        searchPanel.add(btnDelete);
        searchPanel.add(btnToggleFavorite);

        // 테이블
        String[] columnNames = {"제목", "아티스트", "앨범", "발매일", "즐겨찾기"};
        musicTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        musicTable = new JTable(musicTableModel);
        musicTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(musicTable);

        // 재생 컨트롤
        btnPlay = new JButton("▶");
        btnPrevious = new JButton("⏮");
        btnNext = new JButton("⏭");

        JPanel controlPanel = new JPanel();
        controlPanel.add(btnPrevious);
        controlPanel.add(btnPlay);
        controlPanel.add(btnNext);

        topPanel.add(inputPanel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 즐겨찾기 칼럼 폭
        musicTable.getColumnModel().getColumn(4).setMaxWidth(55);
        musicTable.getColumnModel().getColumn(4).setMinWidth(45);
        musicTable.getColumnModel().getColumn(4).setPreferredWidth(52);

        // 리스너 연결
        btnAdd.addActionListener(e -> addMusic());
        btnSearch.addActionListener(e -> searchMusic());
        btnDelete.addActionListener(e -> deleteSelectedMusic());
        btnPlay.addActionListener(e -> playSelected());
        btnPrevious.addActionListener(e -> playPrevious());
        btnNext.addActionListener(e -> playNext());
        btnToggleFavorite.addActionListener(e -> toggleFavoriteView());

        // 즐겨찾기 클릭
        musicTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = musicTable.getSelectedRow();
                int col = musicTable.getSelectedColumn();
                if (row != -1 && col == 4) {
                    toggleFavorite(row);
                }
            }
        });

        refreshTable();
        setVisible(true);
    }

    // 음악 등록
    private void addMusic() {
        String title_ = title.getText().trim();
        String artist_ = artist.getText().trim();
        String album_ = album.getText().trim();
        String year_ = year.getText().trim();

        if (title_.isEmpty() || artist_.isEmpty() || album_.isEmpty() || year_.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 값을 입력해 주세요.");
            return;
        }

        // 파일 선택
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        Music music = new Music(title_, artist_, album_, year_, filePath);
        musicList.add(music);
        refreshTable();

        title.setText("");
        artist.setText("");
        album.setText("");
        year.setText("");
    }

    // 음악 삭제
    private void deleteSelectedMusic() {
        int row = musicTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 음악을 선택하세요.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Music musicToRemove = displayedList.get(row);
            musicList.remove(musicToRemove);
            refreshTable();
        }
    }

    // 검색 기능
    private void searchMusic() {
        String keyword = search.getText().trim().toLowerCase();
        musicTableModel.setRowCount(0);
        displayedList.clear();

        for (Music m : musicList) {
            if (m.getTitle().toLowerCase().contains(keyword) ||
                    m.getArtist().toLowerCase().contains(keyword) ||
                    m.getAlbum().toLowerCase().contains(keyword)) {
                displayedList.add(m);
                musicTableModel.addRow(new Object[]{
                        m.getTitle(),
                        m.getArtist(),
                        m.getAlbum(),
                        m.getYear(),
                        m.isFavorite() ? "❤️" : "🤍"
                });
            }
        }
        statusLabel.setText("검색 결과 (" + displayedList.size() + "곡)");
        isFavoriteMode = false;
    }

    // 즐겨찾기 토글
    private void toggleFavorite(int row) {
        if (row >= 0 && row < displayedList.size()) {
            Music m = displayedList.get(row);
            m.setFavorite(!m.isFavorite());
            musicTableModel.setValueAt(m.isFavorite() ? "❤️" : "🤍", row, 4);
        }
    }

    // 즐겨찾기/전체 목록 보기
    private void toggleFavoriteView() {
        isFavoriteMode = !isFavoriteMode;
        refreshTable();
    }

    // 테이블 새로고침
    private void refreshTable() {
        musicTableModel.setRowCount(0);
        displayedList.clear();

        if (isFavoriteMode) {
            int favoriteCount = 0;
            for (Music m : musicList) {
                if (m.isFavorite()) {
                    displayedList.add(m);
                    musicTableModel.addRow(new Object[]{
                            m.getTitle(),
                            m.getArtist(),
                            m.getAlbum(),
                            m.getYear(),
                            m.isFavorite() ? "❤️" : "🤍"
                    });
                    favoriteCount++;
                }
            }
            statusLabel.setText("즐겨찾기 (" + favoriteCount + "곡)");
        } else {
            for (Music m : musicList) {
                displayedList.add(m);
                musicTableModel.addRow(new Object[]{
                        m.getTitle(),
                        m.getArtist(),
                        m.getAlbum(),
                        m.getYear(),
                        m.isFavorite() ? "❤️" : "🤍"
                });
            }
            statusLabel.setText("전체 목록 (" + musicList.size() + "곡)");
        }
    }

    // 선택한 곡 재생
    private void playSelected() {
        int row = musicTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "재생할 음악을 선택하세요.");
            return;
        }
        currentIndex = row;
        Music selectedMusic = displayedList.get(row);
        playMusic(selectedMusic);
    }

    // 음악 재생 , 멈춤
    private void playMusic(Music m) {
        stopMusic();

        playThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(m.getFilePath())) {
                player = new Player(fis);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "음악 파일을 재생할 수 없습니다.");
            }
        });
        playThread.start();

        JOptionPane.showMessageDialog(this, "재생 중: " + m.getTitle());
    }

    // 이전곡
    private void playPrevious() {
        if (currentIndex <= 0) {
            JOptionPane.showMessageDialog(this, "이전 곡이 없습니다.");
            return;
        }
        currentIndex--;
        musicTable.setRowSelectionInterval(currentIndex, currentIndex);
        playMusic(displayedList.get(currentIndex));
    }

    // 다음곡
    private void playNext() {
        if (currentIndex >= musicTable.getRowCount() - 1) {
            JOptionPane.showMessageDialog(this, "다음 곡이 없습니다.");
            return;
        }
        currentIndex++;
        musicTable.setRowSelectionInterval(currentIndex, currentIndex);
        playMusic(displayedList.get(currentIndex));
    }

    // 음악 정지
    private void stopMusic() {
        if (player != null) {
            player.close();
        }
        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }
    }

    public static void main(String[] args) {
        new PlayList();
    }
}